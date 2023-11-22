package org.freeplane.main.codeexplorermode;

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
	final private JavaPackage javaPackage;
    static final String UI_ICON_NAME = "code_package";

	public PackageNodeModel(final JavaPackage javaPackage, final MapModel map, String text, int subgroupIndex) {
		super(map, subgroupIndex);
		this.javaPackage = javaPackage;
		Set<JavaPackage> subpackages = javaPackage.getSubpackages();
		setFolded(! subpackages.isEmpty());
		long classCount = javaPackage.getClassesInPackageTree().stream()
		        .filter(CodeNodeModel::isNamed)
		        .count();
		setID(javaPackage.getName());
		setText(text + formatClassCount(classCount));
	}

	@Override
	public List<NodeModel> getChildren() {
		initializeChildNodes();
		return super.getChildren();
	}

	protected boolean initializeChildNodes() {
	    List<NodeModel> children = super.getChildrenInternal();
	    if (!children.isEmpty())
	        return false;
	    final Set<JavaPackage> packages = javaPackage.getSubpackages();
	    boolean hasSubpackages = ! packages.isEmpty();
	    boolean hasClasses = ! javaPackage.getClasses().isEmpty();
	    if(! hasSubpackages)
	        return false;
	    GraphNodeSort<JavaPackage> childNodes = new GraphNodeSort<JavaPackage>();
	    for (JavaPackage childPackage : packages) {
	        childNodes.addNode(childPackage);
	        Map<JavaPackage, Long> dependencies = childPackage.getClassDependenciesFromThisPackageTree().stream()
	                .collect(Collectors.groupingBy(this::getTargetChildPackage, Collectors.counting()));
	        dependencies.entrySet().stream()
	        .filter(e -> e.getKey().getParent().isPresent())
	        .forEach(e -> childNodes.addEdge(childPackage, e.getKey(), e.getValue()));
	    }
	    if(hasClasses) {
	        childNodes.addNode(javaPackage);
	        Map<JavaPackage, Long> dependencies = javaPackage.getClassDependenciesFromThisPackage().stream()
	                .collect(Collectors.groupingBy(this::getTargetChildPackage, Collectors.counting()));
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

    private CodeNodeModel createChildPackageNode(JavaPackage childPackage, String parentName, int subgroupIndex) {
        String childPackageName = childPackage.getRelativeName();
        Set<JavaPackage> subpackages = childPackage.getSubpackages();
        if(childPackage == javaPackage || subpackages.isEmpty() && ! childPackage.getClasses().isEmpty()) {
            return new ClassesNodeModel(childPackage, getMap(), childPackage == javaPackage, subgroupIndex);
        }
        else if(subpackages.size() == 1 && childPackage.getClasses().isEmpty())
            return createChildPackageNode(subpackages.iterator().next(), parentName + childPackageName + ".", subgroupIndex);
        else
            return new PackageNodeModel(childPackage, getMap(), parentName + childPackageName, subgroupIndex);
    }

    private JavaPackage getTargetChildPackage(Dependency dep) {
        JavaClass targetClass = dep.getTargetClass();
        return getChildPackage(targetClass);
    }

    private JavaPackage getChildPackage(JavaClass javaClass) {
        JavaPackage subpackage = javaClass.getPackage();
        for(;;) {
            Optional<JavaPackage> parent = subpackage.getParent();
            if(! parent.isPresent() || parent.get().equals(javaPackage))
                return subpackage;
            subpackage = parent.get();
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
