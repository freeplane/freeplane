package org.freeplane.plugin.codeexplorer.map;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;


class PackageNodeModel extends CodeNodeModel {
    static final String UI_ICON_NAME = "code_package";

    private static boolean containsAnalyzedClassesInPackageTree(JavaPackage javaPackage) {
        return javaPackage.getClassesInPackageTree().stream().anyMatch(CodeNodeModel::isClassSourceKnown);
    }

    private final JavaPackage javaPackage;
    private final long classCount;

	public PackageNodeModel(final JavaPackage javaPackage, final MapModel map, String text, int subgroupIndex) {
		super(map, subgroupIndex);
		this.javaPackage = javaPackage;
		this.classCount = javaPackage.getClassesInPackageTree().stream()
		        .filter(CodeNodeModel::isClassSourceKnown)
		        .filter(CodeNodeModel::isNamed)
		        .count();
		setID(javaPackage.getName());
		setText(text + formatClassCount(classCount));
        setFolded(classCount > 0);
	}

	@Override
	public List<NodeModel> getChildren() {
		initializeChildNodes();
		return super.getChildren();
	}

	@Override
    protected boolean initializeChildNodes() {
	    List<NodeModel> children = super.getChildrenInternal();
	    if (!children.isEmpty()|| classCount == 0)
	        return false;
	    final List<JavaPackage> packages = relevantSubpackages(javaPackage);
	    boolean hasSubpackages = ! packages.isEmpty();
	    boolean hasClasses = javaPackage.getClasses().stream().anyMatch(CodeNodeModel::isClassSourceKnown);
	    if(! hasSubpackages)
	        return false;
	    GraphNodeSort<JavaPackage> childNodes = new GraphNodeSort<JavaPackage>();
	    for (JavaPackage childPackage : packages) {
	        childNodes.addNode(childPackage);
	        DistinctTargetDependencyFilter filter = new DistinctTargetDependencyFilter();
	        Map<JavaPackage, Long> dependencies = childPackage.getClassDependenciesFromThisPackageTree().stream()
	                .filter(dep -> dep.getTargetClass().getSource().isPresent())
	                .map(filter::knownDependency)
	                .collect(Collectors.groupingBy(this::getTargetChildNodePackage, Collectors.counting()));
	        dependencies.entrySet().stream()
	        .filter(e -> e.getKey().getParent().isPresent())
	        .forEach(e -> childNodes.addEdge(childPackage, e.getKey(), e.getValue()));
	    }
	    if(hasClasses) {
	        childNodes.addNode(javaPackage);
	        DistinctTargetDependencyFilter filter = new DistinctTargetDependencyFilter();
	        Map<JavaPackage, Long> dependencies = javaPackage.getClassDependenciesFromThisPackage().stream()
	                .map(filter::knownDependency)
	                .collect(Collectors.groupingBy(this::getTargetChildNodePackage, Collectors.counting()));
	        dependencies.entrySet().stream()
	        .filter(e -> e.getKey().getParent().isPresent())
	        .forEach(e -> childNodes.addEdge(javaPackage, e.getKey(), e.getValue()));
	    }

	    List<List<JavaPackage>> orderedPackages = childNodes.sortNodes();
	    for(int subgroupIndex = 0; subgroupIndex < orderedPackages.size(); subgroupIndex++) {
	        for (JavaPackage childPackage : orderedPackages.get(subgroupIndex)) {
	            final CodeNodeModel node = createChildPackageNode(childPackage, "", subgroupIndex);
	            children.add(node);
	            node.setParent(this);
	        }
	    }
	    return true;
	}

    private static List<JavaPackage> relevantSubpackages(JavaPackage javaPackage) {
        return javaPackage.getSubpackages()
	            .stream()
	            .filter(PackageNodeModel::containsAnalyzedClassesInPackageTree)
	            .collect(Collectors.toList());
    }

    private CodeNodeModel createChildPackageNode(JavaPackage childPackage, String parentName, int subgroupIndex) {
        String childPackageName = childPackage.getRelativeName();
        List<JavaPackage> subpackages = relevantSubpackages(childPackage);
        if(childPackage == javaPackage || subpackages.isEmpty() && ! childPackage.getClasses().isEmpty()) {
            return new ClassesNodeModel(childPackage, getMap(), childPackage == javaPackage, subgroupIndex);
        }
        else if(subpackages.size() == 1 && childPackage.getClasses().isEmpty())
            return createChildPackageNode(subpackages.iterator().next(), parentName + childPackageName + ".", subgroupIndex);
        else
            return new PackageNodeModel(childPackage, getMap(), parentName + childPackageName, subgroupIndex);
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
	public int getChildCount(){
	    int knownCount = super.getChildrenInternal().size();
	    if(knownCount > 0)
	        return knownCount;
	    else
	        return javaPackage.getSubpackages().size() + (javaPackage.getClasses().isEmpty() ? 0 : 1);
	}



    @Override
	protected List<NodeModel> getChildrenInternal() {
    	initializeChildNodes();
    	return super.getChildrenInternal();
	}

	@Override
	public boolean hasChildren() {
    	return ! javaPackage.getSubpackages().isEmpty();
	}


    @Override
	public String toString() {
		return getText();
	}

    @Override
    Set<JavaClass> getClassesInPackageTree() {
        return javaPackage.getClassesInPackageTree();
    }

    @Override
    Stream<Dependency> getOutgoingDependencies() {
        return javaPackage.getClassDependenciesFromThisPackageTree().stream();
    }

    @Override
    Stream<Dependency> getIncomingDependencies() {
        return javaPackage.getClassDependenciesToThisPackageTree().stream();
    }

    @Override
    String getUIIconName() {
        return UI_ICON_NAME;
    }

    @Override
    Set<CodeNodeModel> findCyclicDependencies() {
        CodeNodeModel classes = (CodeNodeModel) getMap().getNodeForID(getID() + ".package");
        if(classes != null)
            return classes.findCyclicDependencies();
        else
            return super.findCyclicDependencies();
    }
}
