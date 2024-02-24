/*
 * Created on 9 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.freeplane.core.extension.Configurable;
import org.freeplane.core.extension.IExtension;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.icon.NamedIcon;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.codeexplorer.CodeModeController;
import org.freeplane.plugin.codeexplorer.connectors.CodeLinkController;
import org.freeplane.plugin.codeexplorer.dependencies.CodeDependency;
import org.freeplane.plugin.codeexplorer.task.AnnotationMatcher;
import org.freeplane.plugin.codeexplorer.task.DependencyJudge;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.domain.properties.HasName;

public abstract class CodeNode extends NodeModel {

    protected final int subprojectIndex;

    static String formatClassCount(long classCount) {
        return " (" + classCount + (classCount == 1 ? " class)" : " classes)");
    }

    public static JavaClass findEnclosingNamedClass(JavaClass javaClass) {
        if (javaClass.isAnonymousClass())
            return findEnclosingNamedClass(javaClass.getEnclosingClass().get());
        else
            if(javaClass.isArray())
                return javaClass.getBaseComponentType();
            else
                return javaClass;
    }

    public static boolean hasValidTopLevelClass(JavaClass javaClass) {
        if(javaClass.isArray())
            return hasValidTopLevelClass(javaClass.getBaseComponentType());
        if(javaClass.isTopLevelClass())
            return true;
        for(JavaClass enclosingClass = javaClass.getEnclosingClass().get();;
                enclosingClass = javaClass.getEnclosingClass().get()) {
            if(! enclosingClass.getSource().equals(javaClass.getSource()))
                return false;
            if(enclosingClass.isTopLevelClass())
                return true;
        }
    }

    public static boolean hasValidTopLevelClasses(Dependency dependency) {
        return hasValidTopLevelClass(dependency.getOriginClass()) && hasValidTopLevelClass(dependency.getTargetClass());
    }

    static String idWithSubprojectIndex(String idWithoutIndex, int subprojectIndex) {
        return idWithoutIndex + "[" + subprojectIndex + "]";
    }

    static JavaClass getTargetNodeClass(Dependency dependency) {
        return findEnclosingNamedClass(dependency.getTargetClass());
    }

    static boolean isNamed(JavaClass jc) {
        return ! jc.isAnonymousClass() && ! jc.isArray();
    }

    private static boolean isTargetSourceKnown(Dependency dep) {
        return isClassSourceKnown(dep.getTargetClass());
    }

    private static boolean isOriginSourceKnown(Dependency dep) {
        return isClassSourceKnown(dep.getOriginClass());
    }

    static boolean isClassSourceKnown(JavaClass javaClass) {
        return javaClass.getSource().isPresent();
    }


    static boolean classesBelongToTheSamePackage(JavaClass first, JavaClass second) {
        return second.getPackage().equals(first.getPackage());
    }

    static boolean classesBelongToTheSamePackage(Dependency dependency) {
        return classesBelongToTheSamePackage(dependency.getOriginClass(), dependency.getTargetClass());
    }

    CodeNode(CodeMap map,  int subprojectIndex) {
        super(map);
        this.subprojectIndex = subprojectIndex;
    }

    void updateAnnotations(AnnotationMatcher annotationMatcher) {
        NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(this);
        for(int row = attributes.getRowCount() - 1; row >= 0; row--) {
            if(attributes.getAttribute(row) instanceof AnnotationAttribute) {
                attributes.getAttributes().remove(row);
                attributes.fireTableRowsDeleted(this, row, row);
            }
        }

        if(! annotationMatcher.isEmpty()) {
            getAnnotations().forEach(annotation -> {
                String annotationName = ClassNode.nodeText(annotation.getRawType());
                annotation.getProperties().entrySet().stream()
                .filter(attributeEntry -> annotationMatcher.matches(annotation, attributeEntry.getKey()))
                .forEach(attributeEntry -> addAnnotationAttributes(attributes, "@" +annotationName, attributeEntry.getKey(), attributeEntry.getValue()));
                if(annotation.getProperties().isEmpty()
                        && annotationMatcher.matches(annotation.getRawType()))
                    addAnnotationAttributes(attributes, "@" + annotationName, "value", "");
            });
            getInterfaces().forEach(javaInterface -> {
                final JavaClass javaClass = javaInterface.toErasure();
                String interfaceName = ClassNode.nodeText(javaClass);
                 if(annotationMatcher.matches(javaClass))
                    addAnnotationAttributes(attributes, "interface", "value", interfaceName);
            });
         }

        getChildren().forEach(child -> ((CodeNode)child).updateAnnotations(annotationMatcher));

    }

    private void addAnnotationAttributes(NodeAttributeTableModel attributes, String annotationName,
            String key, Object values) {
        Stream<?> valueStream =  values.getClass().isArray() ? Stream.of((Object[])values) : Stream.of(values);
        valueStream.forEach(value ->
            attributes.addRowNoUndo(this, new AnnotationAttribute(key.equals("value") ? annotationName : annotationName + "." + key, value)));
    }

    Set<? extends JavaAnnotation<? extends HasName>> getAnnotations(){
        return Collections.emptySet();
    }


    Set<JavaType> getInterfaces(){
        return Collections.emptySet();
    }

    @Override
    public CodeMap getMap() {
         return (CodeMap) super.getMap();
    }

    @Override
    public CodeNode getParentNode() {
        return (CodeNode) super.getParentNode();
    }


    String getCodeElementName() {
        return getCodeElement().getName();
    }

    void setIdWithIndex(String idWithoutIndex) {
        setID(idWithSubprojectIndex(idWithoutIndex));
    }

    String idWithSubprojectIndex(String idWithoutIndex) {
        return idWithSubprojectIndex(idWithoutIndex, subprojectIndex);
    }

    boolean belongsToSameSubproject(JavaClass javaClass) {
        return hasValidTopLevelClass(javaClass) && validClassBelongsToSameSubproject(javaClass);
    }

    private boolean validClassBelongsToSameSubproject(JavaClass javaClass) {
        int anotherSubprojectIndex = subprojectIndexOf(javaClass);
        return anotherSubprojectIndex == subprojectIndex;
    }

    int subprojectIndexOf(JavaClass javaClass) {
        return getMap().subprojectIndexOf(javaClass);
    }

    boolean belongsToOtherSubproject(JavaClass javaClass) {
        return hasValidTopLevelClass(javaClass) && ! validClassBelongsToSameSubproject(javaClass);
    }

    abstract HasName getCodeElement();


    Set<CodeNode> findCyclicDependencies() {return Collections.emptySet();}
    abstract Stream<Dependency> getOutgoingDependencies();
    abstract Stream<Dependency> getIncomingDependencies();
    abstract String getUIIconName();

    Stream<Dependency> getIncomingAndOutgoingDependencies(){
        return Stream.concat(getIncomingDependencies(), getOutgoingDependencies());
    }

    public Stream<Dependency> getOutgoingDependenciesWithKnownTargets(){
        return getOutgoingDependencies().filter(CodeNode::isTargetSourceKnown);
    }
    public Stream<Dependency> getIncomingDependenciesWithKnownOrigins(){
        return getIncomingDependencies().filter(CodeNode::isOriginSourceKnown);
    }
    Stream<Dependency> getIncomingAndOutgoingDependenciesWithKnownTargets(){
        return Stream.concat(getIncomingDependenciesWithKnownOrigins(), getOutgoingDependenciesWithKnownTargets());
    }

    public Stream<Dependency> getOutgoingDependenciesWithKnownTargets(Filter  filter){
        return getOutgoingDependenciesWithKnownTargets()
                .filter(dep -> getMap().getNodeByClass(dep.getOriginClass()).isVisible(filter));
    }

    public Stream<Dependency> getIncomingDependenciesWithKnownOrigins(Filter  filter){
        return getIncomingDependenciesWithKnownOrigins()
                .filter(dep -> getMap().getNodeByClass(dep.getTargetClass()).isVisible(filter));
    }

    Stream<Dependency> getIncomingAndOutgoingDependenciesWithKnownTargets(Filter  filter){
        return Stream.concat(getIncomingDependenciesWithKnownOrigins(filter), getOutgoingDependenciesWithKnownTargets(filter));
    }

    @Override
    public List<NamedIcon> getIcons() {
        UIIcon uiIcon = IconStoreFactory.ICON_STORE.getUIIcon(getUIIconName());
        return Collections.singletonList(uiIcon);
    }

    public Stream<CodeDependency> outgoingCodeDependenciesWithKnownTargets() {
        MemoizedCodeDependencies extension = getExtension(MemoizedCodeDependencies.class);
        if(extension != null)
            return extension.outgoing();
        if(getParentNode() == null || getParentNode().getChildCount() <= 40)
            return collectOutgoingCodeDependenciesWithKnownTargets();
        return memoizeCodeDependencies().outgoing();
    }

    private Stream<CodeDependency> collectOutgoingCodeDependenciesWithKnownTargets() {
        return getOutgoingDependenciesWithKnownTargets().parallel().map(getMap()::toCodeDependency);
    }

    public Stream<CodeDependency> incomingCodeDependenciesWithKnownOrigins() {
        MemoizedCodeDependencies extension = getExtension(MemoizedCodeDependencies.class);
        if(extension != null)
            return extension.incoming();
        if(getParentNode() == null || getParentNode().getChildCount() <= 40)
            return collectIncomingCodeDependenciesWithKnownOrigins();
        return memoizeCodeDependencies().incoming();
    }

    MemoizedCodeDependencies memoizeCodeDependencies() {
        MemoizedCodeDependencies extension;
        extension = new MemoizedCodeDependencies();
        addExtension(extension);
        return extension;
    }
    private Stream<CodeDependency> collectIncomingCodeDependenciesWithKnownOrigins() {
        return getIncomingDependenciesWithKnownOrigins().parallel().map(getMap()::toCodeDependency);
    }

    public static Optional<String> classSourceLocationOf(JavaClass javaClass) {
        return javaClass.getSource()
                .map(s -> {
                    URI uri = s.getUri();
                    String path = uri.getRawPath();
                    String classLocation = path != null ?  path : uri.getSchemeSpecificPart();
                    String classSourceLocation = classLocation.substring(0, classLocation.length() - javaClass.getName().length() - ".class".length());
                    return classSourceLocation;
                });
    }

    private class MemoizedCodeDependencies implements IExtension{
        DependencyJudge judge;
        CodeDependency[] incoming;
        CodeDependency[] outgoing;
        MemoizedCodeDependencies() {
            update();
        }

        private void update() {
            DependencyJudge currentJudge = getMap().getJudge();
            if (judge != currentJudge) {
                judge = currentJudge;
                incoming = collectIncomingCodeDependenciesWithKnownOrigins().toArray(CodeDependency[]::new);
                outgoing = collectOutgoingCodeDependenciesWithKnownTargets().toArray(CodeDependency[]::new);
            }
        }

        Stream<CodeDependency> incoming() {
            update();
            return Stream.of(incoming).parallel();
        }

        Stream<CodeDependency> outgoing() {
            update();
            return Stream.of(outgoing).parallel();
        }
    }

    @Override
    public <T extends IExtension> T getExtension(Class<T> clazz) {
        if (NodeLinks.class.equals(clazz)) {
            ModeController controller = Controller.getCurrentModeController();
            if(! controller.getModeName().equals(CodeModeController.MODENAME))
                return null;
            Configurable mapViewComponent = controller.getController().getMapViewManager().getMapViewConfiguration();
            if(mapViewComponent != null) {
                return (T) new CodeNodeLinks((CodeLinkController) controller.getExtension(LinkController.class), mapViewComponent, this);
            }
        }
       return super.getExtension(clazz);
    }

    void setInitialFoldingState() {
        if(getParentNode().getChildCount() > 1 && getChildCount() > 0)
            setFolded(true);
    }
}
