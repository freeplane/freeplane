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
package org.freeplane.io.url.mindmapmode;

import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.freeplane.controller.Freeplane;
import org.freeplane.map.clipboard.mindmapmode.MClipboardController;
import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.mindmapmode.MMapController;
import org.freeplane.modes.ModeControllerAction;
import org.freeplane.modes.mindmapmode.MModeController;

class ImportBranchAction extends ModeControllerAction {
	public ImportBranchAction(final MModeController modeController) {
		super(modeController, "import_branch");
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel parent = getModeController().getSelectedNode();
		if (parent == null) {
			return;
		}
		final JFileChooser chooser = new JFileChooser();
		final FileFilter fileFilter = ((FileManager) getModeController()
		    .getUrlManager()).getFileFilter();
		if (fileFilter != null) {
			chooser.addChoosableFileFilter(fileFilter);
		}
		final int returnVal = chooser.showOpenDialog(Freeplane.getController()
		    .getViewController().getContentPane());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				final MapModel map = parent.getMap();
				final NodeModel node = ((MMapController) getMModeController()
				    .getMapController()).loadTree(map, chooser
				    .getSelectedFile());
				((MClipboardController) getMModeController()
				    .getClipboardController()).paste(node, parent);
			}
			catch (final Exception ex) {
				getModeController().getUrlManager().handleLoadingException(ex);
			}
		}
	}
}
