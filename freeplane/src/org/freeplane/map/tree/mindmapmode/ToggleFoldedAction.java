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
package org.freeplane.map.tree.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.ListIterator;

import javax.swing.Action;

import org.freeplane.controller.Freeplane;
import org.freeplane.controller.resources.ResourceController;
import org.freeplane.main.Tools;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.ModeControllerAction;
import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.modes.mindmapmode.actions.instance.ActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.instance.CompoundActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.instance.FoldActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.undo.ActionPair;
import deprecated.freemind.modes.mindmapmode.actions.undo.IActor;

class ToggleFoldedAction extends ModeControllerAction implements IActor {
	public ToggleFoldedAction(final MModeController controller) {
		super(controller, "toggle_folded");
		controller.getActionFactory().registerActor(this, getDoActionClass());
	}

	public void act(final ActionInstance action) {
		if (action instanceof FoldActionInstance) {
			final FoldActionInstance foldAction = (FoldActionInstance) action;
			final MModeController modeController = getMModeController();
			final NodeModel node = modeController.getMapController()
			    .getNodeFromID(foldAction.getNode());
			final boolean fold = foldAction.getFolded();
			modeController.getMapController()._setFolded(node, fold);
			if (Freeplane.getController().getResourceController()
			    .getBoolProperty(
			        ResourceController.RESOURCES_SAVE_FOLDING_STATE)) {
				modeController.getMapController().nodeChanged(node);
			}
		}
	}

	public void actionPerformed(final ActionEvent e) {
		toggleFolded();
	}

	private CompoundActionInstance createFoldAction(
	                                                final ListIterator iterator,
	                                                final boolean fold,
	                                                final boolean undo) {
		final CompoundActionInstance comp = new CompoundActionInstance();
		for (final ListIterator it = iterator; it.hasNext();) {
			final NodeModel node = (NodeModel) it.next();
			final FoldActionInstance foldAction = createSingleFoldAction(fold,
			    node, undo);
			if (foldAction != null) {
				if (!undo) {
					comp.addChoice(foldAction);
				}
				else {
					comp.addAtChoice(0, foldAction);
				}
			}
		}
		return comp;
	}

	/**
	 * @return null if node cannot be folded.
	 */
	private FoldActionInstance createSingleFoldAction(final boolean fold,
	                                                  final NodeModel node,
	                                                  final boolean undo) {
		FoldActionInstance foldAction = null;
		final ModeController modeController = getModeController();
		if ((undo && (modeController.getMapController().isFolded(node) == fold))
		        || (!undo && (node.getModeController().getMapController()
		            .isFolded(node) != fold))) {
			if (node.getModeController().getMapController().hasChildren(node)
			        || Tools.safeEquals(Freeplane.getController()
			            .getResourceController().getProperty(
			                "enable_leaves_folding"), "true")) {
				foldAction = new FoldActionInstance();
				foldAction.setFolded(fold);
				foldAction.setNode(modeController.getMapController().getNodeID(
				    node));
			}
		}
		return foldAction;
	}

	public Class getDoActionClass() {
		return FoldActionInstance.class;
	}

	/**
	 */
	public void setFolded(final NodeModel node, final boolean folded) {
		final FoldActionInstance doAction = createSingleFoldAction(folded,
		    node, false);
		final FoldActionInstance undoAction = createSingleFoldAction(!folded,
		    node, true);
		if (doAction == null || undoAction == null) {
			return;
		}
		final MModeController modeController = getMModeController();
		modeController.getActionFactory().startTransaction(
		    (String) getValue(Action.NAME));
		modeController.getActionFactory().executeAction(
		    new ActionPair(doAction, undoAction));
		modeController.getActionFactory().endTransaction(
		    (String) getValue(Action.NAME));
	}

	public void toggleFolded() {
		toggleFolded(getModeController().getSelectedNodes().listIterator());
	}

	public void toggleFolded(final ListIterator listIterator) {
		final boolean fold = getModeController().getMapController()
		    .getFoldingState(Tools.resetIterator(listIterator));
		final CompoundActionInstance doAction = createFoldAction(Tools
		    .resetIterator(listIterator), fold, false);
		final CompoundActionInstance undoAction = createFoldAction(Tools
		    .resetIterator(listIterator), !fold, true);
		final MModeController modeController = getMModeController();
		modeController.getActionFactory().startTransaction(
		    (String) getValue(Action.NAME));
		modeController.getActionFactory().executeAction(
		    new ActionPair(doAction, undoAction));
		modeController.getActionFactory().endTransaction(
		    (String) getValue(Action.NAME));
	}
}
