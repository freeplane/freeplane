/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package accessories.plugins;

import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Iterator;

import org.freeplane.controller.Controller;
import org.freeplane.main.Tools;
import org.freeplane.map.tree.MapController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.mindmapmode.IMouseWheelEventHandler;
import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.extensions.IHookRegistration;
import deprecated.freemind.modes.mindmapmode.hooks.MindMapNodeHookAdapter;

/**
 * @author foltin
 */
public class UnfoldAll extends MindMapNodeHookAdapter {
	public static class Registration implements IHookRegistration,
	        IMouseWheelEventHandler {
		final private MModeController controller;
		final private UnfoldAll hookInstance;

		public Registration(final ModeController controller) {
			this.controller = (MModeController) controller;
			hookInstance = new UnfoldAll();
			hookInstance.setController(controller);
		}

		public void deRegister() {
			controller.deRegisterMouseWheelEventHandler(this);
		}

		public boolean handleMouseWheelEvent(final MouseWheelEvent e) {
			if ((e.getModifiers() & InputEvent.ALT_MASK) != 0) {
				final NodeModel rootNode = Controller.getController().getMap()
				    .getRootNode();
				if (e.getWheelRotation() > 0) {
					hookInstance.unfoldOneStage(rootNode);
				}
				else {
					controller.select(controller.getMapView().getRoot());
					hookInstance.foldOneStage(rootNode);
				}
				return true;
			}
			return false;
		}

		public void register() {
			controller.addMouseWheelEventHandler(this);
		}
	}

	/**
	 *
	 */
	public UnfoldAll() {
		super();
	}

	protected int depth(final NodeModel node) {
		if (node.isRoot()) {
			return 0;
		}
		return depth((NodeModel) node.getParent()) + 1;
	}

	/**
	 */
	protected void foldAll(final NodeModel node) {
		final MapController modeController = node.getModeController()
		    .getMapController();
		for (final Iterator i = modeController.childrenUnfolded(node); i
		    .hasNext();) {
			foldAll((NodeModel) i.next());
		}
		setFolded(node, true);
	}

	/**
	 * Unfolds every node that has only children which themselves have children.
	 * As this function is a bit difficult to describe and perhaps not so
	 * useful, it is currently not introduced into the menus.
	 *
	 * @param node
	 *            node to start from.
	 */
	public void foldLastBranches(final NodeModel node) {
		boolean nodeHasChildWhichIsLeave = false;
		for (final Iterator i = node.getModeController().getMapController()
		    .childrenUnfolded(node); i.hasNext();) {
			final NodeModel child = (NodeModel) i.next();
			if (child.getChildCount() == 0) {
				nodeHasChildWhichIsLeave = true;
			}
		}
		setFolded(node, nodeHasChildWhichIsLeave);
		for (final Iterator i = node.getModeController().getMapController()
		    .childrenUnfolded(node); i.hasNext();) {
			foldLastBranches((NodeModel) i.next());
		}
	}

	/**
	 */
	protected void foldOneStage(final NodeModel node) {
		foldStageN(node, getMaxDepth(node) - 1);
	}

	public void foldStageN(final NodeModel node, final int stage) {
		final int k = depth(node);
		if (k < stage) {
			setFolded(node, false);
			for (final Iterator i = node.getModeController().getMapController()
			    .childrenUnfolded(node); i.hasNext();) {
				foldStageN((NodeModel) i.next(), stage);
			}
		}
		else {
			foldAll(node);
		}
	}

	/**
	 */
	protected int getMaxDepth(final NodeModel node) {
		if (node.getModeController().getMapController().isFolded(node)
		        || !node.getModeController().getMapController().hasChildren(
		            node)) {
			return depth(node);
		}
		int k = 0;
		for (final Iterator i = node.getModeController().getMapController()
		    .childrenUnfolded(node); i.hasNext();) {
			final int l = getMaxDepth((NodeModel) i.next());
			if (l > k) {
				k = l;
			}
		}
		return k;
	}

	public int getMinDepth(final NodeModel node) {
		if (node.getModeController().getMapController().isFolded(node)) {
			return depth(node);
		}
		if (!node.getModeController().getMapController().hasChildren(node)) {
			return Integer.MAX_VALUE;
		}
		int k = Integer.MAX_VALUE;
		for (final Iterator i = node.getModeController().getMapController()
		    .childrenUnfolded(node); i.hasNext();) {
			final int l = getMinDepth((NodeModel) i.next());
			if (l < k) {
				k = l;
			}
		}
		return k;
	}

	@Override
	public void invoke(NodeModel node) {
		super.invoke(node);
		final boolean foldState = Tools
		    .xmlToBoolean(getResourceString("foldingState"));
		final String foldingType = getResourceString("foldingType");
		final String applyTo = getResourceString("applyTo");
		if ("root".equals(applyTo)) {
			node = getMindMapController().getMapController().getRootNode();
		}
		if (foldingType.equals("All")) {
			if (foldState) {
				foldAll(node);
			}
			else {
				unfoldAll(node);
			}
		}
		else {
			if (foldState) {
				foldOneStage(node);
			}
			else {
				unfoldOneStage(node);
			}
		}
	}

	protected void setFolded(final NodeModel node, final boolean state) {
		if (node.getModeController().getMapController().hasChildren(node)
		        && (node.getModeController().getMapController().isFolded(node) != state)) {
			getMindMapController().getMapController().setFolded(node, state);
		}
	}

	public void unfoldAll(final NodeModel node) {
		setFolded(node, false);
		for (final Iterator i = node.getModeController().getMapController()
		    .childrenUnfolded(node); i.hasNext();) {
			unfoldAll((NodeModel) i.next());
		}
	}

	/**
	 */
	protected void unfoldOneStage(final NodeModel node) {
		int minDepth = getMinDepth(node);
		if (minDepth < Integer.MAX_VALUE) {
			minDepth++;
		}
		unfoldStageN(node, minDepth);
	}

	public void unfoldStageN(final NodeModel node, final int stage) {
		final int k = depth(node);
		if (k < stage) {
			setFolded(node, false);
			for (final Iterator i = node.getModeController().getMapController()
			    .childrenUnfolded(node); i.hasNext();) {
				unfoldStageN((NodeModel) i.next(), stage);
			}
		}
		else {
			foldAll(node);
		}
	}
}
