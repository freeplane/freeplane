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
package org.freeplane.features.mindmapmode.addins;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Iterator;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.ui.IMouseWheelEventHandler;
import org.freeplane.core.ui.MenuBuilder;

/**
 * @author foltin
 */
public class UnfoldAll implements IMouseWheelEventHandler {
	@ActionLocationDescriptor(locations = { "/menu_bar/navigate/folding", "/main_toolbar/folding" }, //
	accelerator = "alt HOME")
	private class FoldAllAction extends AMultipleNodeAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public FoldAllAction() {
			super("FoldAllAction", controller);
		}

		@Override
		public void actionPerformed(final ActionEvent e, final NodeModel node) {
			foldAll(node);
		}
	}

	@ActionLocationDescriptor(locations = { "/menu_bar/navigate/folding", "/main_toolbar/folding" }, //
	accelerator = "alt PAGE_UP")
	private class FoldOneLevelAction extends AMultipleNodeAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public FoldOneLevelAction() {
			super("FoldOneLevelAction", controller);
		}

		@Override
		public void actionPerformed(final ActionEvent e, final NodeModel node) {
			foldOneStage(node);
		}
	}

	@ActionLocationDescriptor(locations = { "/menu_bar/navigate/folding", "/main_toolbar/folding" }, //
	accelerator = "alt END")
	private class UnfoldAllAction extends AMultipleNodeAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public UnfoldAllAction() {
			super("UnfoldAllAction", controller);
		}

		@Override
		public void actionPerformed(final ActionEvent e, final NodeModel node) {
			unfoldAll(node);
		}
	}

	@ActionLocationDescriptor(locations = { "/menu_bar/navigate/folding", "/main_toolbar/folding" }, //
	accelerator = "alt PAGE_DOWN")
	private class UnfoldOneLevelAction extends AMultipleNodeAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public UnfoldOneLevelAction() {
			super("UnfoldOneLevelAction", controller);
		}

		@Override
		public void actionPerformed(final ActionEvent e, final NodeModel node) {
			unfoldOneStage(node);
		}
	}

	final private Controller controller;

	/**
	 *
	 */
	public UnfoldAll(final ModeController modeController) {
		super();
		controller = modeController.getController();
		modeController.getUserInputListenerFactory().addMouseWheelEventHandler(this);
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
		menuBuilder.addAnnotatedAction(new UnfoldAllAction());
		menuBuilder.addAnnotatedAction(new FoldAllAction());
		menuBuilder.addAnnotatedAction(new UnfoldOneLevelAction());
		menuBuilder.addAnnotatedAction(new FoldOneLevelAction());
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
		final MapController modeController = controller.getModeController().getMapController();
		for (final Iterator i = modeController.childrenUnfolded(node); i.hasNext();) {
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
		for (final Iterator i = controller.getModeController().getMapController().childrenUnfolded(node); i.hasNext();) {
			final NodeModel child = (NodeModel) i.next();
			if (child.getChildCount() == 0) {
				nodeHasChildWhichIsLeave = true;
			}
		}
		setFolded(node, nodeHasChildWhichIsLeave);
		for (final Iterator i = controller.getModeController().getMapController().childrenUnfolded(node); i.hasNext();) {
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
			for (final Iterator i = controller.getModeController().getMapController().childrenUnfolded(node); i
			    .hasNext();) {
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
		if (controller.getModeController().getMapController().isFolded(node)
		        || !controller.getModeController().getMapController().hasChildren(node)) {
			return depth(node);
		}
		int k = 0;
		for (final Iterator i = controller.getModeController().getMapController().childrenUnfolded(node); i.hasNext();) {
			final int l = getMaxDepth((NodeModel) i.next());
			if (l > k) {
				k = l;
			}
		}
		return k;
	}

	public int getMinDepth(final NodeModel node) {
		if (controller.getModeController().getMapController().isFolded(node)) {
			return depth(node);
		}
		if (!controller.getModeController().getMapController().hasChildren(node)) {
			return Integer.MAX_VALUE;
		}
		int k = Integer.MAX_VALUE;
		for (final Iterator i = controller.getModeController().getMapController().childrenUnfolded(node); i.hasNext();) {
			final int l = getMinDepth((NodeModel) i.next());
			if (l < k) {
				k = l;
			}
		}
		return k;
	}

	public boolean handleMouseWheelEvent(final MouseWheelEvent e) {
		if ((e.getModifiers() & InputEvent.ALT_MASK) != 0) {
			final NodeModel rootNode = controller.getMap().getRootNode();
			if (e.getWheelRotation() > 0) {
				unfoldOneStage(rootNode);
			}
			else {
				final ModeController modeController = controller.getModeController();
				modeController.getMapController().select(controller.getMap().getRootNode());
				foldOneStage(rootNode);
			}
			return true;
		}
		return false;
	}

	protected void setFolded(final NodeModel node, final boolean state) {
		final MapController mapController = controller.getModeController().getMapController();
		if (mapController.hasChildren(node) && (mapController.isFolded(node) != state)) {
			mapController.setFolded(node, state);
		}
	}

	public void unfoldAll(final NodeModel node) {
		setFolded(node, false);
		for (final Iterator i = controller.getModeController().getMapController().childrenUnfolded(node); i.hasNext();) {
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
			for (final Iterator i = controller.getModeController().getMapController().childrenUnfolded(node); i
			    .hasNext();) {
				unfoldStageN((NodeModel) i.next(), stage);
			}
		}
		else {
			foldAll(node);
		}
	}
}
