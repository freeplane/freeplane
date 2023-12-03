package org.freeplane.plugin.codeexplorer.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.codeexplorer.graph.GraphNodeSort;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.domain.properties.HasName;


class PackageNode extends CodeNode {
    static final String UI_ICON_NAME = "code_package";
    static {
        IconStoreFactory.INSTANCE.createStateIcon(PackageNode.UI_ICON_NAME, "code/folder.svg");
    }
    private final JavaPackage javaPackage;
    private final long classCount;

    public PackageNode(final JavaPackage javaPackage, final CodeMap map, String text, int subprojectIndex) {
        super(map, subprojectIndex);
        this.javaPackage = javaPackage;
        this.classCount = getClassesInTree()
                .filter(CodeNode::isNamed)
                .count();
        setIdWithIndex(javaPackage.getName());
        setText(text + formatClassCount(classCount));
        setFolded(classCount > 0);
        initializeChildNodes();
    }

    private Stream<JavaClass> getClassesInTree() {
        return getClassesInTree(javaPackage);
    }

    private Stream<JavaClass> getClassesInTree(JavaPackage somePackage) {
        return somePackage.getClassesInPackageTree().stream()
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
	    boolean hasClasses = getClasses().anyMatch(x -> true);
	    if(hasClasses)
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

	    List<List<JavaPackage>> orderedPackages = childNodes.sortNodes();
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
        else if(subpackages.size() == 1 && childPackage.getClasses().isEmpty())
            return createChildPackageNode(subpackages.iterator().next(), parentName + childPackageName + ".");
        else
            return new PackageNode(childPackage, getMap(), parentName + childPackageName, subprojectIndex);
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
        return javaPackage.getParent().isPresent() ? getClassDependenciesFromPackageTree(javaPackage) : getSubprojectDependenciesFromPackageTree();
    }

    private Stream<Dependency> getSubprojectDependenciesFromPackageTree() {
        return subprojectClasses()
                .flatMap(javaClass -> javaClass.getDirectDependenciesFromSelf().stream())
                .filter(dep -> ! belongsToSameSubproject(dep.getTargetClass()));
    }

    private Stream<JavaClass> subprojectClasses() {
        return ((CodeMap)getMap()).allClasses()
                .filter(this::belongsToSameSubproject);
    }

    @Override
    Stream<Dependency> getIncomingDependencies() {
        return javaPackage.getParent().isPresent() ? getClassDependenciesToPackageTree(javaPackage) : getSubprojectDependenciesToPackageTree();
    }

    private Stream<Dependency> getSubprojectDependenciesToPackageTree() {
        return subprojectClasses()
                .flatMap(javaClass -> javaClass.getDirectDependenciesToSelf().stream())
                .filter(dep -> ! belongsToSameSubproject(dep.getOriginClass()));
    }

    @Override
    String getUIIconName() {
        return UI_ICON_NAME;
    }

    @Override
    Set<CodeNode> findCyclicDependencies() {
        String id = idWithSubprojectIndex(javaPackage.getName() + ".package");
        CodeNode classes = (CodeNode) getMap().getNodeForID(id);
        if(classes != null)
            return classes.findCyclicDependencies();
        else
            return super.findCyclicDependencies();
    }
}
