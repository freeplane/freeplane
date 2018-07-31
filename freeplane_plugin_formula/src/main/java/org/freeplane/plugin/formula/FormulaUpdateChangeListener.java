package org.freeplane.plugin.formula;

import java.util.List;

import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.explorer.mindmapmode.MapExplorerController;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.text.DetailTextModel;
import org.freeplane.features.text.IContentTransformer;
import org.freeplane.features.url.UrlManager;
import org.freeplane.plugin.script.FormulaUtils;

/** cares for updating formula nodes on change of other nodes. */
public class FormulaUpdateChangeListener implements INodeChangeListener, IMapChangeListener{
	@Override
	public void nodeChanged(NodeChangeEvent event) {
		Object property = event.getProperty();
		// Note: this doesn't mean that other properties are not interesting here (e.g. links, edges, ...)
		// since all these could be referenced by formulas too. It's restricted only to limit the number of updates.
		// ALTERNATIVE: property.getClass() == Class.class && IExtension.class.isAssignableFrom((Class<?>)property)
        if (NodeModel.NODE_TEXT.equals(property) || NodeAttributeTableModel.class.equals(property)
                || NodeModel.NOTE_TEXT.equals(property) || NodeModel.NODE_ICON.equals(property)
                || LogicalStyleModel.class.equals(property) || DetailTextModel.class.equals(property)) {
            nodeChangedImpl(false, event.getNode());
		}
	}

	@Override
	public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
		nodeChangedImpl(true, nodeDeletionEvent.parent);
	}

	@Override
	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		// all formulas dependent on the child via getChildren() are also dependent on its parent
		nodeChangedImpl(true, parent);
	}

	@Override
	public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
		// - all formulas dependent on the child via getChildren() are also dependent on its parent
		// FIXME: is child updated or do we have to force that here?
		nodeChangedImpl(true, nodeMoveEvent.oldParent, nodeMoveEvent.newParent);
	}

	@Override
	public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
	}

	@Override
	public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
	}

	@Override
	public void mapChanged(MapChangeEvent event) {
		if (UrlManager.MAP_URL.equals(event.getProperty()))
		    FormulaUtils.clearCache(event.getMap());
		if (MapExplorerController.GLOBAL_NODES.equals(event.getProperty()))
		    globalNodesChanged(event.getMap());
	}

	private void globalNodesChanged(MapModel map) {
		final ModeController modeController = Controller.getCurrentModeController();
		final List<NodeModel> dependencies = FormulaUtils.manageChangeAndReturnGlobalDependencies(map);
		for (NodeModel dependentNode : dependencies) {
			modeController.getMapController().delayedNodeRefresh(dependentNode, IContentTransformer.class,
			    null, null);
		}
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
			modeController.getMapController().delayedNodeRefresh(dependentNode, IContentTransformer.class,
			    null, null);
		}
	}
}
