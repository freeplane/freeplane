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
package org.freeplane.map.url.mindmapmode;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.JFileChooser;

import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeplaneAction;
import org.freeplane.main.Tools;
import org.freeplane.map.link.mindmapmode.MLinkController;
import org.freeplane.map.text.mindmapmode.MTextController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.mindmapmode.MMapController;

class ImportExplorerFavoritesAction extends FreeplaneAction {
	public ImportExplorerFavoritesAction() {
		super("import_explorer_favorites");
	}

	public void actionPerformed(final ActionEvent e) {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(getModeController().getText("select_favorites_folder"));
		final int returnVal = chooser.showOpenDialog(Controller.getController().getViewController()
		    .getContentPane());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File folder = chooser.getSelectedFile();
			Controller.getController().getViewController().out("Importing Favorites ...");
			importExplorerFavorites(folder, getModeController().getSelectedNode(),/*
																																																 * redisplay=
																																																 */
			true);
			Controller.getController().getViewController().out("Favorites imported.");
		}
	}

	/**
	 */
	private NodeModel addNode(final NodeModel target, final String nodeContent) {
		final NodeModel node = ((MMapController) getModeController().getMapController())
		    .addNewNode(target, target.getChildCount(), target.isNewChildLeft());
		((MTextController) getMModeController().getTextController()).setNodeText(node, nodeContent);
		return node;
	}

	public boolean importExplorerFavorites(final File folder, final NodeModel target,
	                                       final boolean redisplay) {
		boolean favoritesFound = false;
		if (folder.isDirectory()) {
			final File[] list = folder.listFiles();
			for (int i = 0; i < list.length; i++) {
				if (list[i].isDirectory()) {
					final String nodeContent = list[i].getName();
					final NodeModel node = addNode(target, nodeContent);
					final boolean favoritesFoundInSubfolder = importExplorerFavorites(list[i],
					    node, false);
					if (favoritesFoundInSubfolder) {
						favoritesFound = true;
					}
					else {
						((MMapController) getModeController().getMapController()).deleteNode(node);
					}
				}
			}
			for (int i = 0; i < list.length; i++) {
				if (!list[i].isDirectory() && Tools.getExtension(list[i]).equals("url")) {
					favoritesFound = true;
					try {
						final NodeModel node = addNode(target, Tools.removeExtension(list[i]
						    .getName()));
						final BufferedReader in = new BufferedReader(new FileReader(list[i]));
						while (in.ready()) {
							final String line = in.readLine();
							if (line.startsWith("URL=")) {
								((MLinkController) node.getModeController().getLinkController())
								    .setLink(node, line.substring(4));
								break;
							}
						}
					}
					catch (final Exception e) {
						org.freeplane.main.Tools.logException(e);
					}
				}
			}
		}
		if (redisplay) {
			getModeController().getMapController().nodeChanged(target);
		}
		return favoritesFound;
	}
}
