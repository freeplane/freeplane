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
package org.freeplane.features.mindmapmode.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import javax.swing.JFileChooser;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.features.mindmapmode.text.MTextController;

class ImportFolderStructureAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportFolderStructureAction(final Controller controller) {
		super("ImportFolderStructureAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(ResourceBundles.getText("select_folder_for_importing"));
		final ViewController viewController = getController().getViewController();
		final int returnVal = chooser.showOpenDialog(viewController.getContentPane());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File folder = chooser.getSelectedFile();
			viewController.out("Importing folder structure ...");
			try {
				importFolderStructure(folder, getModeController().getMapController().getSelectedNode(),
				/*redisplay=*/true);
			}
			catch (final Exception ex) {
				LogTool.severe(ex);
			}
			viewController.out("Folder structure imported.");
		}
	}

	/**
	 */
	private NodeModel addNode(final NodeModel target, final String nodeContent, final String link) {
		final NodeModel node = ((MMapController) getModeController().getMapController()).addNewNode(target, target
		    .getChildCount(), target.isNewChildLeft());
		((MTextController) TextController.getController(getModeController())).setNodeText(node, nodeContent);
		((MLinkController) LinkController.getController(getModeController())).setLink(node, link, false);
		return node;
	}

	public void importFolderStructure(final File folder, final NodeModel target, final boolean redisplay)
	        throws MalformedURLException {
		final File[] list = folder.listFiles();
		if (list == null) {
			return;
		}
		for (int i = 0; i < list.length; i++) {
			if (list[i].isDirectory()) {
				final NodeModel node = addNode(target, list[i].getName(), list[i].toURI().toString());
				importFolderStructure(list[i], node, false);
			}
		}
		for (int i = 0; i < list.length; i++) {
			if (!list[i].isDirectory()) {
				addNode(target, list[i].getName(), list[i].toURI().toString());
			}
		}
		getModeController().getMapController().setFolded(target, true);
	}
}
