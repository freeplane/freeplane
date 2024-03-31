package org.freeplane.plugin.codeexplorer.map;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.codeexplorer.graph.GraphCycleFinder;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.domain.properties.HasName;
import com.tngtech.archunit.core.domain.Formatters;


public class ClassNode extends CodeNode {
    static {
        IconStoreFactory.INSTANCE.createStateIcon(ClassNode.INTERFACE_ICON_NAME, "code/interface.svg");
        IconStoreFactory.INSTANCE.createStateIcon(ClassNode.ABSTRACT_CLASS_ICON_NAME, "code/classAbstract.svg");
        IconStoreFactory.INSTANCE.createStateIcon(ClassNode.CLASS_ICON_NAME, "code/class.svg");
        IconStoreFactory.INSTANCE.createStateIcon(ClassNode.ENUM_ICON_NAME, "code/enum.svg");
        IconStoreFactory.INSTANCE.createStateIcon(ClassNode.ANNOTATION_ICON_NAME, "code/annotation.svg");
    }    private final JavaClass javaClass;
    private Set<JavaClass> innerClasses;
    static final String ANNOTATION_ICON_NAME = "code_annotation";
    static final String INTERFACE_ICON_NAME = "code_interface";
    static final String ABSTRACT_CLASS_ICON_NAME = "code_abstractClass";
    static final String CLASS_ICON_NAME = "code_class";
    static final String ENUM_ICON_NAME = "code_enum";

	ClassNode(final JavaClass javaClass, final CodeMap map, int groupIndex) {
		super(map, groupIndex);
        this.javaClass = javaClass;
        this.innerClasses = null;
		setFolded(false);
		setIdWithIndex(javaClass.getName());
		String nodeText = classNameWithEnclosingClasses(javaClass);
        setText(nodeText);
	}


    @Override
    Set<? extends JavaAnnotation<? extends HasName>> getAnnotations() {
        return javaClass.getAnnotations();
    }


    @Override
    Set<JavaType> getInterfaces(){
        return javaClass.getInterfaces();
    }

    public static String classNameWithEnclosingClasses(final JavaClass javaClass) {
        String simpleName = getSimpleName(javaClass);
        return javaClass.getEnclosingClass()
                .map(ec -> classNameWithEnclosingClasses(ec) + "." + simpleName)
                .orElse(simpleName);
    }

    public static String getSimpleName(final JavaClass javaClass) {
        String simpleName = javaClass.getSimpleName();
        if(simpleName.isEmpty()) {
            final String fullName = javaClass.getName();
            int lastIndexOfNon$ = fullName.length() - 1;
            while (lastIndexOfNon$ >= 0 && fullName.charAt(lastIndexOfNon$) == '$')
                lastIndexOfNon$--;
            return Formatters.ensureSimpleName(fullName.substring(0, lastIndexOfNon$+1))
                    + fullName.substring(lastIndexOfNon$+1);
        }
        return simpleName;
    }

    @Override
    HasName getCodeElement() {
        return javaClass;
    }

	@Override
	public int getChildCount(){
		return 0;
	}

    @Override
	protected List<NodeModel> getChildrenInternal() {
    	return Collections.emptyList();
	}

    @Override
	public String toString() {
		return getText();
	}

    @Override
    Stream<Dependency> getOutgoingDependencies() {
        return getDependencies(JavaClass::getDirectDependenciesFromSelf)
                .filter(dep -> hasValidTopLevelClass(dep.getTargetClass()));
    }

    @Override
    Stream<Dependency> getIncomingDependencies() {
        return getDependencies(JavaClass::getDirectDependenciesToSelf)
                .filter(dep -> hasValidTopLevelClass(dep.getOriginClass()));
    }

    private Stream<Dependency> getDependencies(Function<? super JavaClass, ? extends Set<Dependency>> mapper) {
        return innerClasses == null ? mapper.apply(javaClass).stream()
                : Stream.concat(Stream.of(javaClass), innerClasses.stream())
                .map(mapper)
                .flatMap(Set::stream)
                .filter(this::connectsDifferentNodes);
    }

    private boolean connectsDifferentNodes(Dependency dep) {
        return findEnclosingNamedClass(dep.getOriginClass()) != findEnclosingNamedClass(dep.getTargetClass());
    }

    void registerInnerClass(JavaClass innerClass) {
        if(innerClass == javaClass)
            return;
        if(innerClasses == null)
            innerClasses = new HashSet<>();
        innerClasses.add(innerClass);
    }


    @Override
    String getUIIconName() {
        if(javaClass.isAnnotation())
            return ANNOTATION_ICON_NAME;
        if(javaClass.isInterface())
            return INTERFACE_ICON_NAME;
        if(javaClass.isEnum())
            return ENUM_ICON_NAME;
        if(javaClass.getModifiers().contains(JavaModifier.ABSTRACT))
            return ABSTRACT_CLASS_ICON_NAME;
        return CLASS_ICON_NAME;
    }

    @Override
    Set<CodeNode> findCyclicDependencies() {
        GraphCycleFinder<CodeNode> cycleFinder = new GraphCycleFinder<CodeNode>();
        cycleFinder.addNode(this);
        cycleFinder.stopSearchHere();
        cycleFinder.exploreGraph(Collections.singleton(this),
                this::connectedTargetNodesInGroup,
                this::connectedOriginNodesInGroup);
        LinkedHashSet<CodeNode> cycles = cycleFinder.findSimpleCycles().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return cycles;
    }

    private Stream<CodeNode> connectedOriginNodesInGroup(CodeNode node) {
        Stream<JavaClass> originClasses = node.getIncomingDependenciesWithKnownOrigins()
        .map(Dependency::getOriginClass);
        return nodesContainedInGroup(originClasses);
    }

    private Stream<CodeNode> connectedTargetNodesInGroup(CodeNode node) {
        Stream<JavaClass> targetClasses = node.getOutgoingDependenciesWithKnownTargets()
        .map(Dependency::getTargetClass);
        return nodesContainedInGroup(targetClasses);
    }
    private Stream<CodeNode> nodesContainedInGroup(Stream<JavaClass> classes) {
        return classes
        .filter(this::belongsToSameGroup)
        .map(CodeNode::findEnclosingNamedClass)
        .map(JavaClass::getName)
        .map(this::idWithGroupIndex)
        .map(getMap()::getNodeForID)
        .map(CodeNode.class::cast);
    }
}
