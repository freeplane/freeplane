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
package org.freeplane.features.common.clipboard;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import org.freeplane.core.actions.IFreeplaneAction;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.ControllerUtil;
import org.freeplane.core.extension.ExtensionContainer;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.ui.AFreeplaneAction;

class CopyAction extends AFreeplaneAction implements IFreeplaneAction {
    private static final long serialVersionUID = 4816549133103377252L;
	private static final String NAME = "copy";

	public CopyAction(final Controller controller) {
		super(controller, NAME, "/images/editcopy.png");
	}

	public void actionPerformed(final ActionEvent e) {
		final Controller controller = getController();
		final ExtensionContainer modeController = getModeController();
		final IMapSelection selection = controller.getSelection();
		if (selection != null) {
			ClipboardController clipboardController = (ClipboardController)modeController.getExtension(ClipboardController.class);
			final Transferable copy = clipboardController.copy(selection);
			if (copy != null) {
				clipboardController.setClipboardContents(copy);
			}
		}
	}

	public String getName() {
	    return NAME;
    }
}
