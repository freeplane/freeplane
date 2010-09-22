package org.freeplane.plugin.spreadsheet;

import java.awt.Component;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.IMapViewChangeListener;
import org.freeplane.core.frame.IMapViewManager;
import org.freeplane.features.common.map.IMapChangeListener;
import org.freeplane.features.common.map.INodeChangeListener;
import org.freeplane.features.common.map.MapChangeEvent;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeChangeEvent;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.plugin.script.FormulaUtils;

/** cares for updating formula nodes on change of other nodes. */
public class FormulaUpdateChangeListener implements INodeChangeListener, IMapChangeListener, IMapViewChangeListener {
	public void nodeChanged(NodeChangeEvent event) {
		if (NodeModel.NODE_TEXT.equals(event.getProperty()))
			nodeChangedImpl(event.getNode());
	}

	public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
		nodeChangedImpl(parent);
	}

	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		// all formulas dependent on the child via getChildren() are also dependent on its parent
		nodeChangedImpl(parent);
	}

	public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
		// - all formulas dependent on the child via getChildren() are also dependent on its parent
		// FIXME: is child updated or do we have to force that here?
		nodeChangedImpl(oldParent, newParent);
	}

	public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
	}

	public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode, int index) {
	}
	
	public void mapChanged(MapChangeEvent event) {
		
	}

	private void nodeChangedImpl(NodeModel... nodes) {
		final ModeController modeController = Controller.getCurrentModeController();
		//FIXME: needed???
		//		if (modeController == null || modeController.isUndoAction()) {
		//			return;
		//		}
		final List<NodeModel> dependencies = FormulaUtils.manageChangeAndReturnDependencies(nodes);
		for (NodeModel dependentNode : dependencies) {
			modeController.getMapController().delayedNodeRefresh(dependentNode, FormulaUpdateChangeListener.class,
			    null, null);
		}
	}

	public void afterViewChange(Component oldView, Component newView) {
    }

	public void afterViewClose(Component mapView) {
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		final MapModel map = mapViewManager.getModel(mapView);
		FormulaUtils.clearCache(map);
    }

	public void afterViewCreated(Component mapView) {
    }

	public void beforeViewChange(Component oldView, Component newView) {
		// initialize a singleton variable here???
    }
}
