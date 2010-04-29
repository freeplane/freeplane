/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.util.List;

import javax.swing.Icon;

import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.frame.ViewController;
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
		final NodeModel nodeModel = ((NodeProxy) center).getDelegate();
		selection.centerNode(nodeModel);
	}

	public Node getSelected() {
		return new NodeProxy(selection.getSelected(), modeController);
	}

	public List<Node> getSelecteds() {
		return ProxyUtils.createNodeList(selection.getSelection(), modeController);
	}

	public List<Node> getSortedSelection(final boolean differentSubtrees) {
		return ProxyUtils.createNodeList(selection.getSortedSelection(differentSubtrees), modeController);
	}

	public void select(final Node toSelect) {
		final NodeModel nodeModel = ((NodeProxy) toSelect).getDelegate();
		selection.selectAsTheOnlyOneSelected(nodeModel);
	}

	public void selectBranch(final Node branchRoot) {
		final NodeModel nodeModel = ((NodeProxy) branchRoot).getDelegate();
		modeController.getMapController().displayNode(nodeModel);
		selection.selectBranch(nodeModel, false);
	}

	public void selectMultipleNodes(final List<Node> toSelect) {
		for (final Node node : toSelect) {
			final NodeModel nodeModel = ((NodeProxy) node).getDelegate();
			if (!selection.isSelected(nodeModel)) {
				selection.toggleSelected(nodeModel);
			}
		}
	}

	public void deactivateUndo() {
		final MapModel map = modeController.getController().getMap();
		if (map instanceof MapModel) {
			modeController.deactivateUndo((MMapModel) map);
		}
	}

	public void setStatusInfo(final String info) {
		final ViewController viewController = getViewController();
		viewController.out(info);
	}

	private ViewController getViewController() {
		return modeController.getController().getViewController();
	}

	public void setStatusInfo(final String key, final String info) {
		final ViewController viewController = getViewController();
		viewController.addStatusInfo(key, info);
	}

	public void setStatusInfo(final String key, final Icon icon) {
		final ViewController viewController = getViewController();
		viewController.addStatusImage(key, icon);
	}

	public List<Node> find(final ICondition condition) {
		return ProxyUtils.find(condition, modeController, modeController.getController().getMap().getRootNode());
	}

	public List<Node> find(final Closure closure) {
		return ProxyUtils.find(closure, modeController, modeController.getController().getMap().getRootNode());
	}
}
