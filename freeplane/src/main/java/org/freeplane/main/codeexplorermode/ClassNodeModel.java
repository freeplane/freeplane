package org.freeplane.main.codeexplorermode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.freeplane.core.extension.Configurable;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeRelativePath;
import org.freeplane.view.swing.map.MapView;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;


class ClassNodeModel extends CodeNodeModel {
	final private JavaClass javaClass;

	public ClassNodeModel(final JavaClass javaClass, final MapModel map) {
		super(map);
		this.javaClass = javaClass;
		setFolded(false);
		setID(javaClass.getName());
		String simpleName = javaClass.getSimpleName();
        setText(simpleName);
	}

	@Override
	public List<NodeModel> getChildren() {
		initializeChildNodes();
		return super.getChildren();
	}

	private void initializeChildNodes() {/**/}

	@Override
	public int getChildCount(){
		return 0;
	}



    @Override
	protected List<NodeModel> getChildrenInternal() {
    	initializeChildNodes();
    	return super.getChildrenInternal();
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
    Collection<CodeConnectorModel> getOutgoingLinks(Configurable component) {
        MapView mapView = (MapView) component;
        Set<Dependency> classDependencies = javaClass.getDirectDependenciesFromSelf();
        Map<String, Long> dependencies = classDependencies.stream()
                .map(dep -> getVisibleTargetName(mapView, dep))
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
}
