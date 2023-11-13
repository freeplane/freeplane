package org.freeplane.main.codeexplorermode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.freeplane.core.extension.Configurable;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeRelativePath;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;


class JavaPackageNodeModel extends CodeNodeModel {
	final private JavaPackage javaPackage;

	public JavaPackageNodeModel(final JavaPackage javaPackage, final MapModel map, String text) {
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
	        if(! packages.isEmpty()) {
	            GraphNodeSort<JavaPackage> preferenceOrdering = new GraphNodeSort<JavaPackage>();
	            for (JavaPackage childPackage : packages) {
	                preferenceOrdering.addNode(childPackage);
	                Map<JavaPackage, Long> dependencies = childPackage.getClassDependenciesFromThisPackageTree().stream()
	                        .collect(Collectors.groupingBy(this::getTargetChildPackage, Collectors.counting()));
	                dependencies.entrySet().stream()
	                .filter(e -> e.getKey().getParent().isPresent())
	                .forEach(e -> preferenceOrdering.addEdge(childPackage, e.getKey(), e.getValue()));
	            }

	            List<JavaPackage> orderedPackages = preferenceOrdering.sortNodes();
                for (JavaPackage childPackage : orderedPackages) {
                    final JavaPackageNodeModel node = createChildPackageNode(childPackage, "");
                    children.add(node);
                    node.setParent(this);
                }
            }
	    }
	}

    private JavaPackageNodeModel createChildPackageNode(JavaPackage childPackage, String parentName) {
        String childPackageName = childPackage.getRelativeName();
        Set<JavaPackage> subpackages = childPackage.getSubpackages();
        if(subpackages.size() == 1 && childPackage.getClasses().isEmpty())
            return createChildPackageNode(subpackages.iterator().next(), parentName + childPackageName + ".");
        else
            return new JavaPackageNodeModel(childPackage, getMap(), parentName + childPackageName);
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
		return javaPackage.getSubpackages().size();
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
        Map<String, Long> dependencies = packageDependencies.stream()
                .map(dep -> getVisibleTargetPackageName(mapView, dep))
                .filter(name -> name != null)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<CodeConnectorModel> connectors = dependencies.entrySet().stream()
            .map(this::createConnector)
            .collect(Collectors.toList());
        return connectors;

    }

    private CodeConnectorModel createConnector(Entry<String, Long> e) {
        String targetId = e.getKey();
        NodeModel target = getMap().getNodeForID(targetId);
        NodeRelativePath nodeRelativePath = new NodeRelativePath(this, target);
        return new CodeConnectorModel(this, targetId, e.getValue().intValue(), nodeRelativePath.compareNodePositions() > 0);
    }

    private boolean includesDependenciesForChildPackages(MapView mapView) {
        return mapView.getNodeView(this).isFolded();
    }

    private String getVisibleTargetPackageName(MapView mapView, Dependency dep) {
        JavaPackage targetPackage = dep.getTargetClass().getPackage();
        return getVisibleTargetPackageName(mapView, targetPackage);
    }

    private String getVisibleTargetPackageName(MapView mapView, JavaPackage targetPackage) {
        for(;;) {
            String targetPackageName = targetPackage.getName();
            NodeModel targetNode = getMap().getNodeForID(targetPackageName);
            if(targetNode != null) {
                NodeView targetView = mapView.getNodeView(targetNode);
                if(targetView != null)
                    return targetPackageName;
            }
            Optional<JavaPackage> parent = targetPackage.getParent();
            if(! parent.isPresent())
                return null;
            targetPackage = parent.get();
        }
    }
}
