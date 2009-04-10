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
package org.freeplane.features.mindmapmode.clipboard;

import java.awt.datatransfer.Transferable;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.features.common.clipboard.ClipboardController;
import org.freeplane.features.mindmapmode.MModeController;

/**
 * @author Dimitry Polivaev
 */
public class MClipboardController extends ClipboardController {
	static private CutAction cut;
	static private PasteAction paste;

	/**
	 * @param modeController
	 */
	public MClipboardController(final MModeController modeController) {
		super(modeController);
		createActions(modeController);
	}

	/**
	 * @param modeController
	 */
	private void createActions(final ModeController modeController) {
		final Controller controller = modeController.getController();
		modeController.addAction(new ExportToHTMLAction(controller));
		modeController.addAction(new ExportBranchToHTMLAction(controller));
		cut = new CutAction(controller);
		modeController.addAction(cut);
		paste = new PasteAction(controller);
		modeController.addAction(paste);
	}

	public Transferable cut(final List nodeList) {
		return cut.cut(nodeList);
	}

	/**
	 * @param isLeft
	 *            determines, whether or not the node is placed on the left or
	 *            right.
	 * @return true, if successfully.
	 **/
	public void paste(final Transferable t, final NodeModel target, final boolean asSibling, final boolean isLeft) {
		((PasteAction) getModeController().getAction("PasteAction")).paste(t, target, asSibling, isLeft);
	}
}
