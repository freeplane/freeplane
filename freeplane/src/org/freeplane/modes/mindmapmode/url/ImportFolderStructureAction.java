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
package org.freeplane.modes.mindmapmode.url;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import javax.swing.JFileChooser;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.core.url.UrlManager;
import org.freeplane.map.link.LinkController;
import org.freeplane.map.link.mindmapmode.MLinkController;
import org.freeplane.map.text.TextController;
import org.freeplane.map.text.mindmapmode.MTextController;
import org.freeplane.modes.mindmapmode.MMapController;
import org.freeplane.modes.mindmapmode.MModeController;

class ImportFolderStructureAction extends FreeplaneAction {
	public ImportFolderStructureAction() {
		super("import_folder_structure");
	}

	public void actionPerformed(final ActionEvent e) {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(getModeController().getText("select_folder_for_importing"));
		final int returnVal = chooser.showOpenDialog(Controller.getController().getViewController()
		    .getContentPane());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File folder = chooser.getSelectedFile();
			Controller.getController().getViewController().out("Importing folder structure ...");
			try {
				importFolderStructure(folder, getModeController().getMapController()
				    .getSelectedNode(),
				/*redisplay=*/true);
			}
			catch (final Exception ex) {
				org.freeplane.core.util.Tools.logException(ex);
			}
			Controller.getController().getViewController().out("Folder structure imported.");
		}
	}

	/**
	 */
	private NodeModel addNode(final NodeModel target, final String nodeContent, final String link) {
		final NodeModel node = ((MMapController) getModeController().getMapController())
		    .addNewNode(target, target.getChildCount(), target.isNewChildLeft());
		((MTextController) TextController.getController(MModeController.getMModeController()))
		    .setNodeText(node, nodeContent);
		((MLinkController) LinkController.getController(MModeController.getMModeController()))
		    .setLink(node, link);
		return node;
	}

	public void importFolderStructure(final File folder, final NodeModel target,
	                                  final boolean redisplay) throws MalformedURLException {
		Logger.global.warning("Entering folder: " + folder);
		if (folder.isDirectory()) {
			final File[] list = folder.listFiles();
			for (int i = 0; i < list.length; i++) {
				if (list[i].isDirectory()) {
					final NodeModel node = addNode(target, list[i].getName(), UrlManager.fileToUrl(
					    list[i]).toString());
					importFolderStructure(list[i], node, false);
				}
			}
			for (int i = 0; i < list.length; i++) {
				if (!list[i].isDirectory()) {
					addNode(target, list[i].getName(), UrlManager.fileToUrl(list[i]).toString());
				}
			}
		}
		getModeController().getMapController().setFolded(target, true);
	}
}
