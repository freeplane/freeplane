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
package org.freeplane.map.clipboard.mindmapmode;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import org.freeplane.controller.Freeplane;
import org.freeplane.map.clipboard.ClipboardController;
import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.ModeControllerAction;

class ExportToHTMLAction extends ModeControllerAction {
	public ExportToHTMLAction(final ModeController controller) {
		super(controller, "export_to_html", null);
	}

	public void actionPerformed(final ActionEvent e) {
		final MapModel map = Freeplane.getController().getMap();
		try {
			final File file = new File(map.getFile() + ".html");
			ClipboardController.saveHTML((NodeModel) map.getRoot(), file);
			getModeController().getMapController().loadURL(file.toString());
		}
		catch (final IOException ex) {
			org.freeplane.main.Tools.logException(ex);
		}
	}
}
