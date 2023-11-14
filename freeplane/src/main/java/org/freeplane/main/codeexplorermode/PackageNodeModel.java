package org.freeplane.main.codeexplorermode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.freeplane.core.extension.Configurable;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.view.swing.map.MapView;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;


class PackageNodeModel extends CodeNodeModel {
	final private JavaPackage javaPackage;

	public PackageNodeModel(final JavaPackage javaPackage, final MapModel map, String text) {
		super(map);
		this.javaPackage = javaPackage;
		Set<JavaPackage> subpackages = javaPackage.getSubpackages();
		setFolded(! subpackages.isEmpty());
		setID(javaPackage.getName());
		setText(text);
	}

	@Override
	public List<NodeModel> getChildren() {
		initializeChildNodes();
		return super.getChildren();
	}

	private void initializeChildNodes() {
	    List<NodeModel> children = super.getChildrenInternal();
	    if (children.isEmpty()) {
	        final Set<JavaPackage> packages = javaPackage.getSubpackages();
	        boolean hasSubpackages = ! packages.isEmpty();
            boolean hasClasses = ! javaPackage.getClasses().isEmpty();
            if(hasSubpackages) {
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

	            List<JavaPackage> orderedPackages = childNodes.sortNodes();
                for (JavaPackage childPackage : orderedPackages) {
                    final CodeNodeModel node = createChildPackageNode(childPackage, "");
                    children.add(node);
                    node.setParent(this);
                }
            }
	    }
	}

    private CodeNodeModel createChildPackageNode(JavaPackage childPackage, String parentName) {
        String childPackageName = childPackage.getRelativeName();
        Set<JavaPackage> subpackages = childPackage.getSubpackages();
        if(childPackage == javaPackage || subpackages.isEmpty() && ! childPackage.getClasses().isEmpty()) {
            return new ClassesNodeModel(childPackage, getMap());
        }
        else if(subpackages.size() == 1 && childPackage.getClasses().isEmpty())
            return createChildPackageNode(subpackages.iterator().next(), parentName + childPackageName + ".");
        else
            return new PackageNodeModel(childPackage, getMap(), parentName + childPackageName);
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
    Collection<CodeConnectorModel> getOutgoingLinks(Configurable component) {
        MapView mapView = (MapView) component;
        Set<Dependency> packageDependencies = includesDependenciesForChildPackages(mapView)
                ? javaPackage.getClassDependenciesFromThisPackageTree()
                        : javaPackage.getClassDependenciesFromThisPackage();
        List<CodeConnectorModel> connectors = toConnectors(packageDependencies, mapView);
        return connectors;
    }

    @Override
    Collection<CodeConnectorModel> getIncomingLinks(Configurable component) {
        MapView mapView = (MapView) component;
        Set<Dependency> packageDependencies = includesDependenciesForChildPackages(mapView)
                ? javaPackage.getClassDependenciesToThisPackageTree()
                        : javaPackage.getClassDependenciesToThisPackage();
        List<CodeConnectorModel> connectors = toConnectors(packageDependencies, mapView);
        return connectors;
    }
}
