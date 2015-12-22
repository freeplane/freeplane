package org.freeplane.features.link;

import java.util.List;

import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.text.IContentTransformer;

/** cares for updating formula nodes on change of other nodes. */
public class NodeUpdateChangeListener implements INodeChangeListener, IMapChangeListener{
	public void nodeChanged(NodeChangeEvent event) {
		Object property = event.getProperty();
		// Note: this doesn't mean that other properties are not interesting here (e.g. links, notes, ...)
		// since all these could be referenced by formulas too. It's restricted to the properties that may
		// contain formulas only to limit the number of updates.
		if (NodeModel.NODE_TEXT.equals(property)) {
			nodeChangedImpl(false, event.getNode());
		}
	}

	public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
		nodeChangedImpl(true, nodeDeletionEvent.parent);
	}

	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		// all formulas dependent on the child via getChildren() are also dependent on its parent
		nodeChangedImpl(true, parent);
	}

	public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
		// - all formulas dependent on the child via getChildren() are also dependent on its parent
		// FIXME: is child updated or do we have to force that here?
		nodeChangedImpl(true, nodeMoveEvent.oldParent, nodeMoveEvent.newParent);
	}

	public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
	}

	public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
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
		final List<NodeModel> dependencies = EvaluationDependencies.manageChangeAndReturnDependencies(includeChanged, nodes);
		for (NodeModel dependentNode : dependencies) {
			modeController.getMapController().delayedNodeRefresh(dependentNode, IContentTransformer.class,
			    null, null);
		}
	}
}
