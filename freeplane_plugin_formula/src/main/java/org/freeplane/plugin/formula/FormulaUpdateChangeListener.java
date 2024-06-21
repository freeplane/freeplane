package org.freeplane.plugin.formula;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.freeplane.features.explorer.MapExplorerController;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.plugin.script.FormulaCache;
import org.freeplane.plugin.script.FormulaDependencies;
import org.freeplane.plugin.script.FormulaUtils;

/** cares for updating formula nodes on change of other nodes. */
public class FormulaUpdateChangeListener implements INodeChangeListener, IMapChangeListener, IMapLifeCycleListener {



	@Override
    public int priority() {
        return 1;
    }

    @Override
	public void nodeChanged(NodeChangeEvent event) {
		if (!FormulaCache.class.equals(event.getProperty())) {
            reevaluateNodeDependencies(false, event.getNode());
		}
	}

    @Override
    public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
        NodeModel child = nodeDeletionEvent.node;
        refreshChangedClones(child);
    }

    private void refreshChangedClones(NodeModel node) {
        if(node.isCloneNode()) {
            reevaluateNodeDependencies(true, node);
        }
    }

    @Override
    public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
        reevaluateNodeDependencies(true, nodeDeletionEvent.parent);
    }

	@Override
	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		// all formulas dependent on the child via getChildren() are also dependent on its parent
		reevaluateNodeDependencies(true, parent);
		refreshChangedClones(child);
	}

	private void reevaluateNodeDependencies(boolean includeChanged, NodeModel parent) {
	    reevaluateNodeDependencies(includeChanged, Collections.singletonList(parent));
    }

    @Override
	public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
		// - all formulas dependent on the child via getChildren() are also dependent on its parent
		// FIXME: is child updated or do we have to force that here?
		reevaluateNodeDependencies(true, Arrays.asList(nodeMoveEvent.oldParent, nodeMoveEvent.newParent));
	}

	@Override
	public void mapChanged(MapChangeEvent event) {
		if (UrlManager.MAP_URL.equals(event.getProperty()))
		    FormulaUtils.clearCache(event.getMap());
		if (MapExplorerController.GLOBAL_NODES.equals(event.getProperty()))
		    globalNodesChanged(event.getMap());
	}

	private void globalNodesChanged(MapModel map) {
		final List<NodeModel> dependencies = FormulaDependencies.manageChangeAndReturnGlobalDependencies(map);
		refresh(dependencies);
	}

	private void refresh(final List<NodeModel> dependencies) {
		final ModeController modeController = Controller.getCurrentModeController();
		for (NodeModel dependentNode : dependencies) {
			modeController.getMapController().delayedNodeRefresh(dependentNode, FormulaCache.class,
			    null, null);
		}
	}

	/** in case of insert we look for dependencies of the parent. But the parent is not actually changed in this case.
	 * So there won't be any updates on the parent, even if it has formula that needs an update due to the
	 * changed children count. */
	private void reevaluateNodeDependencies(boolean includeChanged, Collection<NodeModel> nodes) {
		final List<NodeModel> dependencies = FormulaDependencies.manageChangeAndReturnDependencies(includeChanged, nodes);
		refresh(dependencies);
	}

	@Override
	public void onRemove(MapModel map) {
		final List<NodeModel> dependencies = FormulaDependencies.removeAndReturnMapDependencies(map);
		refresh(dependencies);
	}


}
