package org.freeplane.plugin.formula;

import java.awt.Component;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.IMapViewChangeListener;
import org.freeplane.core.frame.IMapViewManager;
import org.freeplane.features.common.attribute.NodeAttributeTableModel;
import org.freeplane.features.common.map.IMapChangeListener;
import org.freeplane.features.common.map.INodeChangeListener;
import org.freeplane.features.common.map.MapChangeEvent;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeChangeEvent;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.ITextTransformer;
import org.freeplane.plugin.script.FormulaUtils;

/** cares for updating formula nodes on change of other nodes. */
public class FormulaUpdateChangeListener implements INodeChangeListener, IMapChangeListener, IMapViewChangeListener {
	public void nodeChanged(NodeChangeEvent event) {
		Object property = event.getProperty();
		// Note: this doesn't mean that other properties are not interesting here (e.g. links, notes, ...)
		// since all these could be referenced by formulas too. It's restricted to the properties that may
		// contain formulas only to limit the number of updates.
		if (NodeModel.NODE_TEXT.equals(property) || NodeAttributeTableModel.class.equals(property)
		        || NodeModel.NOTE_TEXT.equals(property)) {
			nodeChangedImpl(false, event.getNode());
		}
	}

	public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
		nodeChangedImpl(true, parent);
	}

	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		// all formulas dependent on the child via getChildren() are also dependent on its parent
		nodeChangedImpl(true, parent);
	}

	public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
		// - all formulas dependent on the child via getChildren() are also dependent on its parent
		// FIXME: is child updated or do we have to force that here?
		nodeChangedImpl(true, oldParent, newParent);
	}

	public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
	}

	public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode, int index) {
	}
	
	public void mapChanged(MapChangeEvent event) {
		
	}

	/** in case of insert we look for dependencies of the parent. But the parent is not actually changed in this case.
	 * So there won't be any updates on the parent, even if it has formula that needs an update due to the 
	 * changed children count. */
	private void nodeChangedImpl(boolean includeChanged, NodeModel... nodes) {
		final ModeController modeController = Controller.getCurrentModeController();
		//FIXME: needed???
		//		if (modeController == null || modeController.isUndoAction()) {
		//			return;
		//		}
		final List<NodeModel> dependencies = FormulaUtils.manageChangeAndReturnDependencies(includeChanged, nodes);
		for (NodeModel dependentNode : dependencies) {
			modeController.getMapController().delayedNodeRefresh(dependentNode, ITextTransformer.class,
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
