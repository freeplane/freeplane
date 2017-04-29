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
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.JFileChooser;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.ui.ViewController;

class ImportFolderStructureAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportFolderStructureAction() {
		super("ImportFolderStructureAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(TextUtils.getText("select_folder_for_importing"));
		final ViewController viewController = Controller.getCurrentController().getViewController();
		final int returnVal = chooser.showOpenDialog(viewController.getCurrentRootComponent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File folder = chooser.getSelectedFile();
			viewController.out("Importing folder structure ...");
			try {
				importFolderStructure(folder, Controller.getCurrentModeController().getMapController().getSelectedNode(),
				/*redisplay=*/true);
			}
			catch (final Exception ex) {
				LogUtils.severe(ex);
			}
			viewController.out("Folder structure imported.");
		}
	}

	/**
	 */
	private NodeModel addNode(final NodeModel target, final String nodeContent, final String link) {
		final NodeModel node = ((MMapController) Controller.getCurrentModeController().getMapController()).addNewNode(target, target
		    .getChildCount(), target.isNewChildLeft());
		((MTextController) TextController.getController()).setNodeText(node, nodeContent);
		((MLinkController) LinkController.getController()).setLink(node, link, LinkController.LINK_ABSOLUTE);
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
		Controller.getCurrentModeController().getMapController().fold(target);
	}
}
