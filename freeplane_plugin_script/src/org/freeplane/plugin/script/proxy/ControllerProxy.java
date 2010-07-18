/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.util.List;

import javax.swing.Icon;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.IMapSelection;
import org.freeplane.core.frame.ViewController;
import org.freeplane.features.common.filter.condition.ICondition;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.map.MMapModel;
import org.freeplane.plugin.script.proxy.Proxy.Node;

class ControllerProxy implements Proxy.Controller {
// 	final private MModeController modeController;
	final private IMapSelection selection;

	public ControllerProxy() {
		super();
		selection = Controller.getCurrentController().getSelection();
	}

	public void centerOnNode(final Node center) {
		final NodeModel nodeModel = ((NodeProxy) center).getDelegate();
		selection.centerNode(nodeModel);
	}

	public Node getSelected() {
		return new NodeProxy(selection.getSelected());
	}

	public List<Node> getSelecteds() {
		return ProxyUtils.createNodeList(selection.getSelection());
	}

	public List<Node> getSortedSelection(final boolean differentSubtrees) {
		return ProxyUtils.createNodeList(selection.getSortedSelection(differentSubtrees));
	}

	public void select(final Node toSelect) {
		final NodeModel nodeModel = ((NodeProxy) toSelect).getDelegate();
		selection.selectAsTheOnlyOneSelected(nodeModel);
	}

	public void selectBranch(final Node branchRoot) {
		final NodeModel nodeModel = ((NodeProxy) branchRoot).getDelegate();
		Controller.getCurrentController().getModeController().getMapController().displayNode(nodeModel);
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
		MModeController modeController = ((MModeController)Controller.getCurrentController().getModeController());
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
		return Controller.getCurrentController().getViewController();
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
		MModeController modeController = ((MModeController)Controller.getCurrentController().getModeController());
		return ProxyUtils.find(condition, modeController.getController().getMap().getRootNode());
	}

	public List<Node> find(final Closure closure) {
		MModeController modeController = ((MModeController)Controller.getCurrentController().getModeController());
		return ProxyUtils.find(closure, modeController.getController().getMap().getRootNode());
	}
}
