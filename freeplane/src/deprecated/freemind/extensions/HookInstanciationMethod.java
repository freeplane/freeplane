/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is to be deleted.
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
package deprecated.freemind.extensions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.freeplane.controller.Controller;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.ModeController;

public class HookInstanciationMethod {
	private static class AllDestinationNodesGetter implements
	        IDestinationNodesGetter {
		private void addChilds(final NodeModel node,
		                       final Collection allNodeCollection) {
			allNodeCollection.add(node);
			for (final Iterator i = node.getModeController().getMapController()
			    .childrenFolded(node); i.hasNext();) {
				final NodeModel child = (NodeModel) i.next();
				addChilds(child, allNodeCollection);
			}
		}

		public NodeModel getCenterNode(final ModeController controller,
		                               final NodeModel focussed,
		                               final List selecteds) {
			return focussed;
		}

		public Collection getDestinationNodes(final ModeController controller,
		                                      final NodeModel focussed,
		                                      final List selecteds) {
			final Vector returnValue = new Vector();
			addChilds(Controller.getController().getMap().getRootNode(),
			    returnValue);
			return returnValue;
		}
	}

	private static class DefaultDestinationNodesGetter implements
	        IDestinationNodesGetter {
		public NodeModel getCenterNode(final ModeController controller,
		                               final NodeModel focussed,
		                               final List selecteds) {
			return focussed;
		}

		public Collection getDestinationNodes(final ModeController controller,
		                                      final NodeModel focussed,
		                                      final List selecteds) {
			return selecteds;
		}
	}

	private static interface IDestinationNodesGetter {
		NodeModel getCenterNode(ModeController controller, NodeModel focussed,
		                        List selecteds);

		Collection getDestinationNodes(ModeController controller,
		                               NodeModel focussed, List selecteds);
	}

	private static class RootDestinationNodesGetter implements
	        IDestinationNodesGetter {
		public NodeModel getCenterNode(final ModeController controller,
		                               final NodeModel focussed,
		                               final List selecteds) {
			return Controller.getController().getMap().getRootNode();
		}

		public Collection getDestinationNodes(final ModeController controller,
		                                      final NodeModel focussed,
		                                      final List selecteds) {
			final Vector returnValue = new Vector();
			returnValue.add(Controller.getController().getMap().getRoot());
			return returnValue;
		}
	}

	/**
	 * This is for MindMapHooks that wish to be applied to root, whereevery they
	 * are called from. Here, no undo- or redoaction are performed, the undo
	 * information is given by the actions the hook performs.
	 */
	static final public HookInstanciationMethod ApplyToRoot = new HookInstanciationMethod(
	    false, false, new RootDestinationNodesGetter(), false);
	static final public HookInstanciationMethod Once = new HookInstanciationMethod(
	    true, true, new DefaultDestinationNodesGetter(), true);
	/** Each (or none) node should have the hook. */
	static final public HookInstanciationMethod OnceForAllNodes = new HookInstanciationMethod(
	    true, true, new AllDestinationNodesGetter(), true);
	/** The hook should only be added/removed to the root node. */
	static final public HookInstanciationMethod OnceForRoot = new HookInstanciationMethod(
	    true, true, new RootDestinationNodesGetter(), true);
	/**
	 * This is for MindMapHooks in general. Here, no undo- or redoaction are
	 * performed, the undo information is given by the actions the hook
	 * performs.
	 */
	static final public HookInstanciationMethod Other = new HookInstanciationMethod(
	    false, false, new DefaultDestinationNodesGetter(), false);

	static final public HashMap getAllInstanciationMethods() {
		final HashMap res = new HashMap();
		res.put("Once", HookInstanciationMethod.Once);
		res.put("OnceForRoot", HookInstanciationMethod.OnceForRoot);
		res.put("OnceForAllNodes", HookInstanciationMethod.OnceForAllNodes);
		res.put("Other", HookInstanciationMethod.Other);
		res.put("ApplyToRoot", HookInstanciationMethod.ApplyToRoot);
		return res;
	}

	final private IDestinationNodesGetter getter;
	final private boolean isPermanent;
	final private boolean isSingleton;
	final private boolean isUndoable;

	private HookInstanciationMethod(final boolean isPermanent,
	                                final boolean isSingleton,
	                                final IDestinationNodesGetter getter,
	                                final boolean isUndoable) {
		this.isPermanent = isPermanent;
		this.isSingleton = isSingleton;
		this.getter = getter;
		this.isUndoable = isUndoable;
	}

	/**
	 */
	public NodeModel getCenterNode(final ModeController controller,
	                               final NodeModel focussed,
	                               final List selecteds) {
		return getter.getCenterNode(controller, focussed, selecteds);
	}

	/**
	 */
	public Collection getDestinationNodes(final ModeController controller,
	                                      final NodeModel focussed,
	                                      final List selecteds) {
		return getter.getDestinationNodes(controller, focussed, selecteds);
	}

	/**
	 * @return Returns the isPermanent.
	 */
	public boolean isPermanent() {
		return isPermanent;
	}

	public boolean isSingleton() {
		return isSingleton;
	}

	/**
	 */
	public boolean isUndoable() {
		return isUndoable;
	}
}
