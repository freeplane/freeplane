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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFileChooser;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.features.mindmapmode.text.MTextController;

class ImportExplorerFavoritesAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportExplorerFavoritesAction(final Controller controller) {
		super("ImportExplorerFavoritesAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(ResourceBundles.getText("select_favorites_folder"));
		final int returnVal = chooser.showOpenDialog(getController().getViewController().getContentPane());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File folder = chooser.getSelectedFile();
			getController().getViewController().out("Importing Favorites ...");
			importExplorerFavorites(folder, getModeController().getMapController().getSelectedNode(),
			/*redisplay=*/true);
			getController().getViewController().out("Favorites imported.");
		}
	}

	/**
	 */
	private NodeModel addNode(final NodeModel target, final String nodeContent) {
		final NodeModel node = ((MMapController) getModeController().getMapController()).addNewNode(target, target
		    .getChildCount(), target.isNewChildLeft());
		((MTextController) TextController.getController(getModeController())).setNodeText(node, nodeContent);
		return node;
	}

	public boolean importExplorerFavorites(final File folder, final NodeModel target, final boolean redisplay) {
		boolean favoritesFound = false;
		if (folder.isDirectory()) {
			final File[] list = folder.listFiles();
			for (int i = 0; i < list.length; i++) {
				if (list[i].isDirectory()) {
					final String nodeContent = list[i].getName();
					final NodeModel node = addNode(target, nodeContent);
					final boolean favoritesFoundInSubfolder = importExplorerFavorites(list[i], node, false);
					if (favoritesFoundInSubfolder) {
						favoritesFound = true;
					}
					else {
						((MMapController) getModeController().getMapController()).deleteNode(node);
					}
				}
			}
			for (int i = 0; i < list.length; i++) {
				if (!list[i].isDirectory() && UrlManager.getExtension(list[i]).equals("url")) {
					favoritesFound = true;
					BufferedReader in = null;
					try {
						final NodeModel node = addNode(target, UrlManager.removeExtension(list[i].getName()));
						in = new BufferedReader(new FileReader(list[i]));
						String line = null;
						while ( (line = in.readLine()) != null) {
							if (line.startsWith("URL=")) {
								((MLinkController) LinkController.getController(getModeController())).setLink(node,
								    line.substring(4), false);
								break;
							}
						}
					}
					catch (final Exception e) {
						LogTool.severe(e);
					}
					finally {
					    try {
					        if(in != null) {
					            in.close();
					        }
                        } catch (IOException e) {
                            LogTool.warn(e);
                        }
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
