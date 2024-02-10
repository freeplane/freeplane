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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.features.attribute.ManagedAttribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.NodeModel;
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

    public PackageNode(final JavaPackage javaPackage, final CodeMap map, String text, int subprojectIndex, boolean createAttributes) {
        super(map, subprojectIndex);
        this.javaPackage = javaPackage;
        setIdWithIndex(javaPackage.getName());
        this.classCount = getClassesInTree().filter(CodeNode::isNamed).count();
        setText(text + formatClassCount(classCount));
        hasOwnClasses = getClasses().anyMatch(x -> true);
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
        setFolded(getChildCount() >= 2);
    }

    private Stream<JavaClass> getClassesInTree() {
        return getClassesInTree(javaPackage);
    }

    private Stream<JavaClass> getClassesInTree(JavaPackage somePackage) {
        return somePackage.getClassesInPackageTree().stream()
                .filter(CodeNode::hasValidTopLevelClass)
                .filter(this::belongsToSameSubproject);
    }
    private Stream<JavaClass> getClasses() {
        return getClasses(javaPackage);
    }

    private Stream<JavaClass> getClasses(JavaPackage somePackage) {
        return somePackage.getClasses().stream()
                .filter(this::belongsToSameSubproject);
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
	                .filter(dep -> belongsToSameSubproject(dep.getTargetClass()));
            Map<JavaPackage, Long> dependencies = packageDependencies
	                .map(filter::knownDependency)
	                .collect(Collectors.groupingBy(this::getTargetChildNodePackage, Collectors.counting()));
	        dependencies.entrySet().stream()
	        .filter(e -> e.getKey().getParent().isPresent())
	        .forEach(e -> childNodes.addEdge(childPackage, e.getKey(), e.getValue()));
	    }

	    Comparator<Set<JavaPackage>> comparingByReversedClassCount = Comparator.comparing(
            childPackages -> -childPackages.stream()
            .mapToLong(x -> (x == javaPackage ? getClasses() : getClassesInTree(x))
                    .count()).sum()
        );
        List<List<JavaPackage>> orderedPackages = childNodes.sortNodes(comparingByReversedClassCount
	            .thenComparing(SubgroupComparator.comparingByName(JavaPackage::getName)));
	    for(int subgroupIndex = 0; subgroupIndex < orderedPackages.size(); subgroupIndex++) {
	        for (JavaPackage childPackage : orderedPackages.get(subgroupIndex)) {
	            final CodeNode node = createChildPackageNode(childPackage, "");
	            children.add(node);
	            node.setParent(this);
	        }
	    }
	}

    private Stream<Dependency> getClassDependenciesFromPackage(JavaPackage somePackage) {
        Set<JavaClass> classesInTree = getClasses(somePackage).collect(Collectors.toSet());
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
            String childName = samePackage ? childPackageName + " - package" : parentName + childPackageName;
            return new ClassesNode(childPackage, getMap(), childName, samePackage, subprojectIndex);
        }
        else if(subpackages.size() == 1 && !getClasses(childPackage).anyMatch(x -> true))
            return createChildPackageNode(subpackages.iterator().next(), parentName + childPackageName + ".");
        else
            return new PackageNode(childPackage, getMap(), parentName + childPackageName, subprojectIndex, false);
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
                : getSubprojectDependenciesFromPackageTree();
    }

    private Stream<Dependency> getSubprojectDependenciesFromPackageTree() {
        return subprojectClasses()
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .filter(dep -> belongsToOtherSubproject(dep.getTargetClass()));
    }

    private Stream<JavaClass> subprojectClasses() {
        return getMap().allClasses()
                .filter(this::belongsToSameSubproject);
    }

    @Override
    Stream<Dependency> getIncomingDependencies() {
        return javaPackage.getParent().isPresent()
                ? getClassDependenciesToPackageTree(javaPackage)
                        .filter(dep -> CodeNode.hasValidTopLevelClass(dep.getOriginClass()))
                : getSubprojectDependenciesToPackageTree();
    }

    private Stream<Dependency> getSubprojectDependenciesToPackageTree() {
        return subprojectClasses()
                .flatMap(javaClass -> javaClass.getDirectDependenciesToSelf().stream())
                .filter(dep -> belongsToOtherSubproject(dep.getOriginClass()));
    }

    @Override
    String getUIIconName() {
        return hasOwnClasses ? UI_SUBPACKAGE_WITH_CLASSES_ICON_NAME :
                javaPackage.getParent().isPresent() ? UI_SUBPACKAGE_WITHOUT_CLASSES_ICON_NAME
                        : UI_ROOT_PACKAGE_ICON_NAME;
    }

    @Override
    Set<CodeNode> findCyclicDependencies() {
        String id = idWithSubprojectIndex(javaPackage.getName() + ".package");
        CodeNode classes = (CodeNode) getMap().getNodeForID(id);
        if(classes != null)
            return classes.findCyclicDependencies();
        else
            return Collections.emptySet();
    }
}
