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
package org.freeplane.features.url.mindmapmode;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.url.UrlManager;

class ImportBranchAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportBranchAction() {
		super("ImportBranchAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel parent = Controller.getCurrentModeController().getMapController().getSelectedNode();
		if (parent == null) {
			return;
		}
		final JFileChooser chooser = UITools.newFileChooser();
		final FileFilter fileFilter = ((MFileManager) UrlManager.getController()).getFileFilter();
		if (fileFilter != null) {
			chooser.addChoosableFileFilter(fileFilter);
		}
		final int returnVal = chooser.showOpenDialog(Controller.getCurrentController().getViewController().getCurrentRootComponent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				final MapModel map = parent.getMap();
				final URL url = map.getURL();
				final NodeModel node = ((MFileManager) UrlManager.getController()).loadTree(map, chooser.getSelectedFile());
				map.setURL(url);
				PersistentNodeHook.removeMapExtensions(node);
				((MMapController) Controller.getCurrentModeController().getMapController()).insertNode(node, parent);
			}
			catch (final Exception ex) {
				UrlManager.getController().handleLoadingException(ex);
			}
		}
	}
}
