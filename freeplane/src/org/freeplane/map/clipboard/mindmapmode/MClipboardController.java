/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.map.clipboard.mindmapmode;

import java.awt.datatransfer.Transferable;
import java.util.List;

import org.freeplane.controller.Freeplane;
import org.freeplane.map.clipboard.ClipboardController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.mindmapmode.MModeController;

/**
 * @author Dimitry Polivaev
 */
public class MClipboardController extends ClipboardController {
	static private boolean actionsCreated = false;
	static private CutAction cut;
	static private PasteAction paste;
	private static final String RESOURCE_UNFOLD_ON_PASTE = "unfold_on_paste";

	/**
	 * @param modeController
	 */
	public MClipboardController(final MModeController modeController) {
		super(modeController);
		createActions(modeController);
		modeController.setNodeDropTargetListener(new MindMapNodeDropListener(
		    modeController));
	}

	/**
	 * @param modeController
	 */
	private void createActions(final MModeController modeController) {
		if (!actionsCreated) {
			actionsCreated = true;
			Freeplane.getController().addAction("exportToHTML",
			    new ExportToHTMLAction(modeController));
			Freeplane.getController().addAction("exportBranchToHTML",
			    new ExportBranchToHTMLAction(modeController));
			cut = new CutAction(modeController);
			Freeplane.getController().addAction("cut", cut);
			paste = new PasteAction(modeController);
			Freeplane.getController().addAction("paste", paste);
		}
	}

	public Transferable cut(final List nodeList) {
		return cut.cut(nodeList);
	}

	public void paste(final NodeModel node, final NodeModel parent) {
		((PasteAction) Freeplane.getController().getAction("paste")).paste(
		    node, parent);
	}

	public void paste(final Transferable t, final NodeModel parent) {
		paste(t, /* target= */parent, /* asSibling= */false, parent
		    .isNewChildLeft());
	}

	/**
	 * @param isLeft
	 *            determines, whether or not the node is placed on the left or
	 *            right.
	 * @return true, if successfully.
	 **/
	public void paste(final Transferable t, final NodeModel target,
	                  final boolean asSibling, final boolean isLeft) {
		final MModeController modeController = (MModeController) target
		    .getModeController();
		if (!asSibling
		        && modeController.getMapController().isFolded(target)
		        && Freeplane.getController().getResourceController()
		            .getBoolProperty(RESOURCE_UNFOLD_ON_PASTE)) {
			modeController.getMapController().setFolded(target, false);
		}
		((PasteAction) Freeplane.getController().getAction("paste")).paste(t,
		    target, asSibling, isLeft);
	}

	public NodeModel pasteXMLWithoutRedisplay(final String pasted,
	                                          final NodeModel target,
	                                          final boolean asSibling,
	                                          final boolean changeSide,
	                                          final boolean isLeft) {
		return paste.pasteXMLWithoutRedisplay(pasted, target, asSibling,
		    changeSide, isLeft);
	}
}
