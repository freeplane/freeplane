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
package org.freeplane.features.mindmapmode.clipboard;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.features.common.clipboard.ClipboardController;

class ExportBranchToHTMLAction extends FreeplaneAction {
	public ExportBranchToHTMLAction(final Controller controller) {
		super(controller, "export_branch_to_html");
	}

	public void actionPerformed(final ActionEvent e) {
		try {
			final File file = File.createTempFile("tmm", ".html");
			ClipboardController.saveHTML(getModeController().getMapController().getSelectedNode(), file);
			getModeController().getMapController().loadURL(file.toString());
		}
		catch (final IOException ex) {
		}
	}
}
