package org.freeplane.plugin.codeexplorer.map;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.codeexplorer.graph.GraphCycleFinder;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.properties.HasName;


public class ClassNodeModel extends CodeNodeModel {
    static {
        IconStoreFactory.INSTANCE.createStateIcon(ClassNodeModel.INTERFACE_ICON_NAME, "code/interface.svg");
        IconStoreFactory.INSTANCE.createStateIcon(ClassNodeModel.ABSTRACT_CLASS_ICON_NAME, "code/classAbstract.svg");
        IconStoreFactory.INSTANCE.createStateIcon(ClassNodeModel.CLASS_ICON_NAME, "code/class.svg");
        IconStoreFactory.INSTANCE.createStateIcon(ClassNodeModel.ENUM_ICON_NAME, "code/enum.svg");
        IconStoreFactory.INSTANCE.createStateIcon(ClassNodeModel.ANNOTATION_ICON_NAME, "code/annotation.svg");
    }    private final JavaClass javaClass;
    private Set<JavaClass> innerClasses;
    static final String ANNOTATION_ICON_NAME = "code_annotation";
    static final String INTERFACE_ICON_NAME = "code_interface";
    static final String ABSTRACT_CLASS_ICON_NAME = "code_abstractClass";
    static final String CLASS_ICON_NAME = "code_class";
    static final String ENUM_ICON_NAME = "code_enum";

	ClassNodeModel(final JavaClass javaClass, final MapModel map, int subgroupIndex) {
		super(map, subgroupIndex);
        this.javaClass = javaClass;
        this.innerClasses = null;
		setFolded(false);
		setID(javaClass.getName());
		String nodeText = nodeText(javaClass);
        setText(nodeText);
	}

    public static String nodeText(final JavaClass javaClass) {
        String simpleName = javaClass.getSimpleName();
        return javaClass.getEnclosingClass()
                .map(ec -> nodeText(ec) + "." + simpleName)
                .orElse(simpleName);
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
	public boolean hasChildren() {
    	return false;
	}


    @Override
	public String toString() {
		return getText();
	}

    @Override
    Set<JavaClass> getClassesInPackageTree() {
        return Collections.singleton(javaClass);
    }

    @Override
    Stream<Dependency> getOutgoingDependencies() {
        return getDependencies(JavaClass::getDirectDependenciesFromSelf);
    }

    @Override
    Stream<Dependency> getIncomingDependencies() {
        return getDependencies(JavaClass::getDirectDependenciesToSelf);
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
        if(javaClass.isInterface())
            return INTERFACE_ICON_NAME;
        if(javaClass.isEnum())
            return ENUM_ICON_NAME;
        if(javaClass.getModifiers().contains(JavaModifier.ABSTRACT))
            return ABSTRACT_CLASS_ICON_NAME;
        if(javaClass.isAnnotation())
            return ANNOTATION_ICON_NAME;
        return CLASS_ICON_NAME;
    }

    @Override
    Set<CodeNodeModel> findCyclicDependencies() {
        GraphCycleFinder<CodeNodeModel> cycleFinder = new GraphCycleFinder<CodeNodeModel>();
        cycleFinder.addNode(this);
        cycleFinder.stopSearchHere();
        cycleFinder.exploreGraph(Collections.singleton(this),
                this::connectedTargetNodesInTheSameScope,
                this::connectedOriginNodesInTheSameScope);
        LinkedHashSet<CodeNodeModel> cycles = cycleFinder.findSimpleCycles().stream().flatMap(List::stream).collect(Collectors.toCollection(LinkedHashSet::new));
        return cycles;
    }

    private Stream<CodeNodeModel> connectedOriginNodesInTheSameScope(CodeNodeModel node) {
        Stream<JavaClass> originClasses = node.getIncomingDependenciesWithKnownTargets()
        .map(Dependency::getOriginClass);
        return nodesContainedInScope(originClasses);
    }

    private Stream<CodeNodeModel> connectedTargetNodesInTheSameScope(CodeNodeModel node) {
        Stream<JavaClass> targetClasses = node.getOutgoingDependenciesWithKnownTargets()
        .map(Dependency::getTargetClass);
        return nodesContainedInScope(targetClasses);
    }
    private Stream<CodeNodeModel> nodesContainedInScope(Stream<JavaClass> originClasses) {
        return originClasses
        .filter(this::isContainedInScope)
        .map(CodeNodeModel::findEnclosingNamedClass)
        .map(JavaClass::getName)
        .map(getMap()::getNodeForID)
        .map(CodeNodeModel.class::cast);
    }

    private boolean isContainedInScope(JavaClass dependencyClass) {
        return dependencyClass.getPackage().equals(javaClass.getPackage());
    }

    @Override
    protected boolean initializeChildNodes() {
        return false;
    }
}
