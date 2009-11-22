/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.AbstractList;
import java.util.List;

import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.model.NodeModel;
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

			@Override
			public Node get(final int index) {
				final NodeModel nodeModel = selection.getSelection().get(index);
				return new NodeProxy(nodeModel, modeController);
			}

			@Override
			public int size() {
				return selection.size();
			}
		};
	}

	public List<Node> getSortedSelection() {
		return new AbstractList<Node>() {

			@Override
			public Node get(final int index) {
				final NodeModel nodeModel = selection.getSortedSelection().get(
						index);
				return new NodeProxy(nodeModel, modeController);
			}

			@Override
			public int size() {
				return selection.size();
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
}