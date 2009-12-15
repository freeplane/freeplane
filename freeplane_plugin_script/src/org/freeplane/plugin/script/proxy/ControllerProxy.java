/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.AbstractList;
import java.util.List;

import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.features.mindmapmode.MMapModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.plugin.script.proxy.Proxy.Node;

class ControllerProxy implements Proxy.Controller {
	final private MModeController modeController;
	final private IMapSelection selection;

	public ControllerProxy(final MModeController modeController) {
		super();
		selection = modeController.getController().getSelection();
		this.modeController = modeController;
	}

	public void centerOnNode(final Node center) {
		final NodeModel nodeModel = ((NodeProxy) center).getNode();
		selection.centerNode(nodeModel);

	}

	public Node getSelected() {
		return new NodeProxy(selection.getSelected(), modeController);
	}

	public List<Node> getSelecteds() {
		return new AbstractList<Node>() {
			private final List<NodeModel> selectionCopy = selection.getSelection();
			@Override
			public Node get(final int index) {
				final NodeModel nodeModel = selectionCopy.get(index);
				return new NodeProxy(nodeModel, modeController);
			}

			@Override
			public int size() {
				return selectionCopy.size();
			}
		};
	}

	public List<Node> getSortedSelection(final boolean differentSubtrees) {
		return new AbstractList<Node>() {
			final private List<NodeModel> sortedSelection = selection.getSortedSelection(differentSubtrees);
			@Override
			public Node get(final int index) {
				final NodeModel nodeModel = sortedSelection.get(index);
				return new NodeProxy(nodeModel, modeController);
			}

			@Override
			public int size() {
				return sortedSelection.size();
			}
		};
	}

	public void select(final Node toSelect) {
		final NodeModel nodeModel = ((NodeProxy) toSelect).getNode();
		selection.selectAsTheOnlyOneSelected(nodeModel);
	}

	public void selectBranch(final Node branchRoot) {
		final NodeModel nodeModel = ((NodeProxy) branchRoot).getNode();
		selection.selectBranch(nodeModel, false);

	}

	public void selectMultipleNodes(final List<Node> toSelect) {
		for (final Node node : toSelect) {
			final NodeModel nodeModel = ((NodeProxy) node).getNode();
			if (!selection.isSelected(nodeModel)) {
				selection.toggleSelected(nodeModel);
			}
		}

	}

	public void deactivateUndo() {
		MapModel map = modeController.getController().getMap();
		if(map instanceof MapModel){
			modeController.deactivateUndo((MMapModel) map);
		}
	}
}