/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.modes.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;

import org.freeplane.controller.Controller;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.view.NodeView;
import org.freeplane.ui.IMenuItemEnabledListener;

import deprecated.freemind.extensions.HookInstanciationMethod;
import deprecated.freemind.extensions.IHookFactory;
import deprecated.freemind.extensions.INodeHook;
import deprecated.freemind.modes.mindmapmode.actions.instance.ActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.instance.HookNodeActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.instance.NodeListMemberActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.undo.IActor;
import deprecated.freemind.modes.mindmapmode.actions.undo.IHookAction;

/**
 * @deprecated
 */
@Deprecated
public class NodeHookAction extends AbstractAction implements IHookAction,
        IActor {
	String _hookName;
	MModeController mMindMapController;

	public NodeHookAction(final String hookName,
	                      final MModeController controller) {
		super(hookName);
		_hookName = hookName;
		mMindMapController = controller;
		controller.getActionFactory().registerActor(this, getDoActionClass());
	}

	public void act(final ActionInstance action) {
		if (action instanceof HookNodeActionInstance) {
			final HookNodeActionInstance hookNodeAction = (HookNodeActionInstance) action;
			final NodeModel selected = getController().getMapController()
			    .getNodeFromID(hookNodeAction.getNode());
			final Vector selecteds = new Vector();
			for (final Iterator i = hookNodeAction.getListNodeListMemberList()
			    .iterator(); i.hasNext();) {
				final NodeListMemberActionInstance node = (NodeListMemberActionInstance) i
				    .next();
				selecteds.add(getController().getMapController().getNodeFromID(
				    node.getNode()));
			}
			invoke(selected, selecteds, hookNodeAction.getHookName());
		}
	}

	public void actionPerformed(final ActionEvent arg0) {
		Controller.getController().getViewController().setWaitingCursor(true);
		invoke(mMindMapController.getSelectedNode(), mMindMapController
		    .getSelectedNodes());
		Controller.getController().getViewController().setWaitingCursor(false);
	}

	public void addHook(final NodeModel focussed, final List selecteds,
	                    final String hookName) {
		invoke(focussed, selecteds, hookName);
	}

	public HookNodeActionInstance createHookNodeAction(
	                                                   final NodeModel focussed,
	                                                   final List selecteds,
	                                                   final String hookName) {
		final HookNodeActionInstance hookNodeAction = new HookNodeActionInstance();
		hookNodeAction.setNode(focussed.createID());
		hookNodeAction.setHookName(hookName);
		for (final Iterator i = selecteds.iterator(); i.hasNext();) {
			final NodeModel node = (NodeModel) i.next();
			final NodeListMemberActionInstance nodeListMember = new NodeListMemberActionInstance();
			nodeListMember.setNode(node.createID());
			hookNodeAction.addNodeListMember(nodeListMember);
		}
		return hookNodeAction;
	}

	/**
	 * @param focussed
	 *            The real focussed node
	 * @param selecteds
	 *            The list of selected nodes
	 * @param adaptedFocussedNode
	 *            The calculated focussed node (if the hook specifies, that the
	 *            hook should apply to root, then this is the root node).
	 * @param destinationNodes
	 *            The calculated list of selected nodes (see last)
	 */
	private void finishInvocation(final NodeModel focussed,
	                              final List selecteds,
	                              final NodeModel adaptedFocussedNode,
	                              final Collection destinationNodes) {
		final NodeView focussedNodeView = mMindMapController
		    .getNodeView(focussed);
		if (focussedNodeView != null) {
			getController().getMapView().selectAsTheOnlyOneSelected(
			    focussedNodeView);
			getController().getMapView().scrollNodeToVisible(focussedNodeView);
			for (final Iterator i = selecteds.iterator(); i.hasNext();) {
				final NodeModel node = (NodeModel) i.next();
				final NodeView nodeView = mMindMapController.getNodeView(node);
				if (nodeView != null) {
					getController().getMapView().makeTheSelected(nodeView);
				}
			}
		}
	}

	public MModeController getController() {
		return mMindMapController;
	}

	public Class getDoActionClass() {
		return HookNodeActionInstance.class;
	}

	/**
	 */
	private IHookFactory getHookFactory() {
		final IHookFactory factory = mMindMapController.getHookFactory();
		return factory;
	}

	/**
	 */
	public String getHookName() {
		return _hookName;
	}

	/**
	 */
	private HookInstanciationMethod getInstanciationMethod(final String hookName) {
		final IHookFactory factory = getHookFactory();
		final HookInstanciationMethod instMethod = factory
		    .getInstanciationMethod(hookName);
		return instMethod;
	}

	public void invoke(final NodeModel focussed, final List selecteds) {
		addHook(focussed, selecteds, _hookName);
	}

	private void invoke(final NodeModel focussed, final List selecteds,
	                    final String hookName) {
		final HookInstanciationMethod instMethod = getInstanciationMethod(hookName);
		final Collection destinationNodes = instMethod.getDestinationNodes(
		    mMindMapController, focussed, selecteds);
		final NodeModel adaptedFocussedNode = instMethod.getCenterNode(
		    mMindMapController, focussed, selecteds);
		for (final Iterator it = destinationNodes.iterator(); it.hasNext();) {
			final NodeModel currentDestinationNode = (NodeModel) it.next();
			final INodeHook hook = mMindMapController.createNodeHook(hookName,
			    currentDestinationNode, Controller.getController().getMap());
			currentDestinationNode.invokeHook(hook);
			finishInvocation(focussed, selecteds, adaptedFocussedNode,
			    destinationNodes);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.controller.MenuItemEnabledListener#isEnabled(javax.swing.JMenuItem
	 * , javax.swing.Action)
	 */
	public boolean isEnabled(final JMenuItem item, final Action action) {
		if (mMindMapController.getMapView() == null) {
			return false;
		}
		final IHookFactory factory = getHookFactory();
		final Object baseClass = factory.getPluginBaseClass(_hookName);
		if (baseClass != null) {
			if (baseClass instanceof IMenuItemEnabledListener) {
				final IMenuItemEnabledListener listener = (IMenuItemEnabledListener) baseClass;
				return listener.isEnabled(item, action);
			}
		}
		return true;
	}
}
