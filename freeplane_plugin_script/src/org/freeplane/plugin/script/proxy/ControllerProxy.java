/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.features.mindmapmode.MMapModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.n3.nanoxml.XMLElement;
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
		return createNodeList(selection.getSelection());
	}

	public List<Node> getSortedSelection(final boolean differentSubtrees) {
		return createNodeList(selection.getSortedSelection(differentSubtrees));
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
		MapModel map = modeController.getController().getMap();
		if (map instanceof MapModel) {
			modeController.deactivateUndo((MMapModel) map);
		}
	}

	public void setStatusInfo(String info){
		ViewController viewController = getViewController();
		viewController.out(info);
	}

	private ViewController getViewController() {
		return modeController.getController().getViewController();
	}

	public void setStatusInfo(String key, String info){
		ViewController viewController = getViewController();
		viewController.addStatusInfo(key, info);
	}
	public void setStatusInfo(String key, Icon icon){
		ViewController viewController = getViewController();
		viewController.addStatusImage(key, icon);
	}

	public List<Node> find(ICondition condition) {
		return createNodeList(findImpl(condition));
	}

	public List<Node> find(final Closure closure) {
		return createNodeList(findImpl(new ICondition() {
			public boolean checkNode(NodeModel node) {
				try {
					final Object result = closure.call(new Object[] { new NodeProxy(node, modeController) });
					if (result == null)
						throw new RuntimeException("find(): closure returned null instead of boolean/Boolean");
					return (Boolean) result;
				}
				catch (ClassCastException e) {
					throw new RuntimeException("find(): closure returned " + e.getMessage()
					        + " instead of boolean/Boolean");
				}
			}

			public JComponent getListCellRendererComponent() {
				return null;
			}

			public void toXml(XMLElement element) {
			}
		}));
	}

	/** finds from root node. */
	private List<NodeModel> findImpl(ICondition condition) {
		return findImpl(condition, modeController.getController().getMap().getRootNode());
	}

	/** finds from any node downwards. */
	@SuppressWarnings("unchecked")
	private List<NodeModel> findImpl(ICondition condition, NodeModel node) {
		// a shortcut for non-matching leaves
		if (node.isLeaf() && !condition.checkNode(node))
			return Collections.EMPTY_LIST;
		List<NodeModel> matches = new ArrayList<NodeModel>();
		if (condition.checkNode(node))
			matches.add(node);
		final Enumeration<NodeModel> children = node.children();
		while (children.hasMoreElements()) {
			final NodeModel child = children.nextElement();
			matches.addAll(findImpl(condition, child));
		}
		return matches;
	}

	private List<Node> createNodeList(final List<NodeModel> list) {
		return new AbstractList<Node>() {
			final private List<NodeModel> nodeModels = list;

			@Override
			public Node get(final int index) {
				final NodeModel nodeModel = nodeModels.get(index);
				return new NodeProxy(nodeModel, modeController);
			}

			@Override
			public int size() {
				return nodeModels.size();
			}
		};
	}
}
