package org.freeplane.plugin.codeexplorer.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.features.attribute.ManagedAttribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.codeexplorer.graph.GraphCycleFinder;
import org.freeplane.plugin.codeexplorer.graph.GraphNodeSort;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.domain.properties.HasName;


class PackageNode extends CodeNode {
    static final String UI_ROOT_PACKAGE_ICON_NAME = "code_root_package";
    static final String UI_SUBPACKAGE_WITHOUT_CLASSES_ICON_NAME = "code_subpackage_without_classes";
    static final String UI_SUBPACKAGE_WITH_CLASSES_ICON_NAME = "code_subpackage_with_classes";
    static {
        IconStoreFactory.INSTANCE.createStateIcon(PackageNode.UI_ROOT_PACKAGE_ICON_NAME, "code/moduleGroup.svg");
        IconStoreFactory.INSTANCE.createStateIcon(PackageNode.UI_SUBPACKAGE_WITHOUT_CLASSES_ICON_NAME, "code/module.svg");
        IconStoreFactory.INSTANCE.createStateIcon(PackageNode.UI_SUBPACKAGE_WITH_CLASSES_ICON_NAME, "code/sourceRoot.svg");
    }
    private final JavaPackage javaPackage;
    private final long classCount;
    private final boolean hasOwnClasses;

    public PackageNode(final JavaPackage javaPackage, final CodeMap map, String text, int groupIndex, boolean createAttributes) {
        super(map, groupIndex);
        this.javaPackage = javaPackage;
        setIdWithIndex(javaPackage.getName());
        this.classCount = getClassesInTree().filter(CodeNode::isNamed).count();
        setText(text + formatClassCount(classCount));
        hasOwnClasses = getPackageClasses(javaPackage).anyMatch(x -> true);
        if(createAttributes) {
            SortedSet<String> classpath = new TreeSet<>();
            getClassesInTree()
                .forEach(jc -> classSourceLocationOf(jc).ifPresent(classpath::add));
            NodeAttributeTableModel attributes = new NodeAttributeTableModel(1 + classpath.size());
            classpath.forEach(path -> attributes.silentlyAddRowNoUndo(this, new ManagedAttribute("Classpath", path)));
            attributes.silentlyAddRowNoUndo(this, new ManagedAttribute("Class count", classCount));
            addExtension(attributes);
        }
        initializeChildNodes();
    }

    private Stream<JavaClass> getClassesInTree() {
        return getClassesInTree(javaPackage);
    }

    private Stream<JavaClass> getClassesInTree(JavaPackage somePackage) {
        return somePackage.getClassesInPackageTree().stream()
                .filter(CodeNode::hasValidTopLevelClass)
                .filter(this::belongsToSameGroup);
    }
    @Override
    protected Stream<JavaClass> getClasses() {
        return getClassesInTree(javaPackage);
    }

    private Stream<JavaClass> getPackageClasses(JavaPackage somePackage) {
        return somePackage.getClasses().stream()
                .filter(this::belongsToSameGroup);
    }
    private List<JavaPackage> relevantSubpackages(JavaPackage somePackage) {
        return somePackage.getSubpackages()
                .stream()
                .filter(this::containsAnalyzedClassesInPackageTree)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean containsAnalyzedClassesInPackageTree(JavaPackage somePackage) {
        return getClassesInTree(somePackage).anyMatch(x -> true);
    }

    long getClassCount() {
        return classCount;
    }

    @Override
    HasName getCodeElement() {
        return javaPackage;
    }

	private void initializeChildNodes() {
	    List<NodeModel> children = super.getChildrenInternal();
	    if (classCount == 0)
	        return;
	    final List<JavaPackage> packages = relevantSubpackages(javaPackage);
	    boolean hasSubpackages = ! packages.isEmpty();
	    if(! hasSubpackages)
	        return ;
	    if(hasOwnClasses)
	        packages.add(javaPackage);
	    GraphNodeSort<JavaPackage> childNodes = new GraphNodeSort<>();
	    for (JavaPackage childPackage : packages) {
	        childNodes.addNode(childPackage);
	        DistinctTargetDependencyFilter filter = new DistinctTargetDependencyFilter();
	        Stream<Dependency> packageDependencies = (childPackage != javaPackage
	                ? getClassDependenciesFromPackageTree(childPackage)
	                        : getClassDependenciesFromPackage(childPackage))
	                .filter(dep -> belongsToSameGroup(dep.getTargetClass()));
            Map<JavaPackage, Long> dependencies = packageDependencies
	                .map(filter::knownDependency)
	                .collect(Collectors.groupingBy(this::getTargetChildNodePackage, Collectors.counting()));
	        dependencies.entrySet().stream()
	        .filter(e -> e.getKey().getParent().isPresent())
	        .forEach(e -> childNodes.addEdge(childPackage, e.getKey(), e.getValue()));
	    }

	    Comparator<Set<JavaPackage>> comparingByReversedClassCount = Comparator.comparing(
            childPackages -> -childPackages.stream()
            .mapToLong(x -> (x == javaPackage ? getPackageClasses(x) : getClassesInTree(x))
                    .count()).sum()
        );
        List<List<JavaPackage>> orderedPackages = childNodes.sortNodes(
                Comparator.comparing(JavaPackage::getName),
                comparingByReversedClassCount
	            .thenComparing(SubgroupComparator.comparingByName(JavaPackage::getName)));
	    for(int subgroupIndex = 0; subgroupIndex < orderedPackages.size(); subgroupIndex++) {
	        for (JavaPackage childPackage : orderedPackages.get(subgroupIndex)) {
	            final CodeNode node = createChildPackageNode(childPackage, "");
	            children.add(node);
	            node.setParent(this);
	        }
	    }
	    for(NodeModel child: children)
	        ((CodeNode) child).setInitialFoldingState();
	}

    private Stream<Dependency> getClassDependenciesFromPackage(JavaPackage somePackage) {
        Set<JavaClass> classesInTree = getPackageClasses(somePackage).collect(Collectors.toSet());
        return getClassDependenciesFrom(classesInTree);
    }

    private Stream<Dependency> getClassDependenciesFromPackageTree(JavaPackage somePackage) {
        Set<JavaClass> classesInTree = getClassesInTree(somePackage).collect(Collectors.toSet());
        return getClassDependenciesFrom(classesInTree);
    }

    private Stream<Dependency> getClassDependenciesFrom(Set<JavaClass> classesInTree) {
        return classesInTree.stream()
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .filter(dependency -> !classesInTree.contains(dependency.getTargetClass()));
    }


    private Stream<Dependency> getClassDependenciesToPackageTree(JavaPackage somePackage) {
        Set<JavaClass> classesInTree = getClassesInTree(somePackage).collect(Collectors.toSet());
        return classesInTree.stream()
                .flatMap(javaClass -> javaClass.getDirectDependenciesToSelf().stream())
                .filter(dependency -> !classesInTree.contains(dependency.getOriginClass()));
    }
    private CodeNode createChildPackageNode(JavaPackage childPackage, String parentName) {
        String childPackageName = childPackage.getRelativeName();
        List<JavaPackage> subpackages = relevantSubpackages(childPackage);
        boolean samePackage = childPackage == javaPackage;
        if(samePackage || subpackages.isEmpty() && ! childPackage.getClasses().isEmpty()) {
            String childName = samePackage ? (childPackageName.isEmpty() ? "default" : childPackageName) + " - package"
                    : parentName + childPackageName;
            return new ClassesNode(childPackage, getMap(), childName, samePackage, groupIndex);
        }
        else if(subpackages.size() == 1 && !getPackageClasses(childPackage).anyMatch(x -> true))
            return createChildPackageNode(subpackages.iterator().next(), parentName + childPackageName + ".");
        else
            return new PackageNode(childPackage, getMap(), parentName + childPackageName, groupIndex, false);
    }

    private JavaPackage getTargetChildNodePackage(Dependency dep) {
        JavaClass targetClass = dep.getTargetClass();
        return getChildNodePackage(targetClass);
    }

    private JavaPackage getChildNodePackage(JavaClass javaClass) {
        JavaPackage childNodePackage = javaClass.getPackage();
        if(childNodePackage.equals(javaPackage))
            return childNodePackage;
        for(;;) {
            Optional<JavaPackage> parent = childNodePackage.getParent();
            if(! parent.isPresent() || parent.get().equals(javaPackage))
                return childNodePackage;
            childNodePackage = parent.get();
        }

    }

    @Override
	public String toString() {
		return getText();
	}

    @Override
    Stream<Dependency> getOutgoingDependencies() {
        return javaPackage.getParent().isPresent()
                ? getClassDependenciesFromPackageTree(javaPackage)
                        .filter(dep -> CodeNode.hasValidTopLevelClass(dep.getTargetClass()))
                : getGroupDependenciesFromPackageTree();
    }

    private Stream<Dependency> getGroupDependenciesFromPackageTree() {
        return groupClasses()
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .filter(dep -> belongsToOtherGroup(dep.getTargetClass()));
    }

    private Stream<JavaClass> groupClasses() {
        return getMap().allClasses()
                .filter(this::belongsToSameGroup);
    }

    @Override
    Stream<Dependency> getIncomingDependencies() {
        return javaPackage.getParent().isPresent()
                ? getClassDependenciesToPackageTree(javaPackage)
                        .filter(dep -> CodeNode.hasValidTopLevelClass(dep.getOriginClass()))
                : getGroupDependenciesToPackageTree();
    }

    private Stream<Dependency> getGroupDependenciesToPackageTree() {
        return groupClasses()
                .flatMap(javaClass -> javaClass.getDirectDependenciesToSelf().stream())
                .filter(dep -> belongsToOtherGroup(dep.getOriginClass()));
    }

    @Override
    String getUIIconName() {
        return hasOwnClasses ? UI_SUBPACKAGE_WITH_CLASSES_ICON_NAME :
                javaPackage.getParent().isPresent() ? UI_SUBPACKAGE_WITHOUT_CLASSES_ICON_NAME
                        : UI_ROOT_PACKAGE_ICON_NAME;
    }



    @Override
    Set<CodeNode> findCyclicDependencies() {
        GraphCycleFinder<CodeNode> cycleFinder = new GraphCycleFinder<CodeNode>();
        cycleFinder.addNode(this);
        cycleFinder.stopSearchHere();
        cycleFinder.exploreGraph(Collections.singleton(this),
                this::connectedTargetNodes,
                this::connectedOriginNodes);
        Set<Entry<CodeNode, CodeNode>> cycles = cycleFinder.findSimpleCycles();

        return cycles.stream().flatMap(edge ->
        edge.getKey().getOutgoingDependenciesWithKnownTargets().flatMap(dep ->
        classNodes(edge, dep.getOriginClass(), dep.getTargetClass())))
        .collect(Collectors.toSet());
    }

    private Stream<? extends CodeNode> classNodes(Entry<CodeNode, CodeNode> edge,
            final JavaClass originClass, final JavaClass targetClass) {
        final String targetId = idWithGroupIndex(targetClass);
        final CodeNode targetNode = (CodeNode) getMap().getNodeForID(targetId);
        if(targetNode.isDescendantOf(edge.getValue())) {
            final String originId = idWithGroupIndex(originClass);
            final CodeNode originNode = (CodeNode) getMap().getNodeForID(originId);
            return Stream.of(originNode, targetNode);
        }
        else
            return Stream.empty();
    }


    private Stream<CodeNode> connectedOriginNodes(CodeNode node) {
        Stream<JavaClass> originClasses = node.getIncomingDependenciesWithKnownOrigins()
        .map(Dependency::getOriginClass);
        return nodes(originClasses);
    }

    private Stream<CodeNode> connectedTargetNodes(CodeNode node) {
        Stream<JavaClass> targetClasses = node.getOutgoingDependenciesWithKnownTargets()
        .map(Dependency::getTargetClass);
        return nodes(targetClasses);
    }

    private Stream<CodeNode> nodes(Stream<JavaClass> classes) {
        return classes
        .map(this::idWithGroupIndex)
        .map(getMap()::getNodeForID)
        .map(node -> node.isDescendantOf(this) ? this : node.getParentNode())
        .map(CodeNode.class::cast);
    }
}
