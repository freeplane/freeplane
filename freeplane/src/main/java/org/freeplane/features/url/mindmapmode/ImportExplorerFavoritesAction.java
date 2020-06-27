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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFileChooser;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;

class ImportExplorerFavoritesAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportExplorerFavoritesAction() {
		super("ImportExplorerFavoritesAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final JFileChooser chooser = UITools.newFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(TextUtils.getText("select_favorites_folder"));
		final int returnVal = chooser.showOpenDialog(Controller.getCurrentController().getViewController().getCurrentRootComponent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File folder = chooser.getSelectedFile();
			Controller.getCurrentController().getViewController().out("Importing Favorites ...");
			importExplorerFavorites(folder, Controller.getCurrentModeController().getMapController().getSelectedNode(),
			/*redisplay=*/true);
			Controller.getCurrentController().getViewController().out("Favorites imported.");
		}
	}

	/**
	 */
	private NodeModel addNode(final NodeModel target, final String nodeContent) {
		final NodeModel node = ((MMapController) Controller.getCurrentModeController().getMapController()).addNewNode(target, target
		    .getChildCount(), target.isNewChildLeft());
		((MTextController) TextController.getController()).setNodeText(node, nodeContent);
		return node;
	}

	public boolean importExplorerFavorites(final File folder, final NodeModel target, final boolean redisplay) {
		boolean favoritesFound = false;
		final File[] list = folder.listFiles();
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				if (list[i].isDirectory()) {
					final String nodeContent = list[i].getName();
					final NodeModel node = addNode(target, nodeContent);
					final boolean favoritesFoundInSubfolder = importExplorerFavorites(list[i], node, false);
					if (favoritesFoundInSubfolder) {
						favoritesFound = true;
					}
					else {
						((MMapController) Controller.getCurrentModeController().getMapController()).deleteNode(node);
					}
				}
			}
			for (int i = 0; i < list.length; i++) {
				if (!list[i].isDirectory() && FileUtils.getExtension(list[i]).equals("url")) {
					favoritesFound = true;
					BufferedReader in = null;
					try {
						final NodeModel node = addNode(target, FileUtils.removeExtension(list[i].getName()));
						in = new BufferedReader(new FileReader(list[i]));
						String line = null;
						while ((line = in.readLine()) != null) {
							if (line.startsWith("URL=")) {
								final String link = line.substring(4);
                                ((MLinkController) LinkController.getController()).setLink(node,LinkController.createURI(link), LinkController.LINK_ABSOLUTE);
								break;
							}
						}
					}
					catch (final Exception e) {
						LogUtils.severe(e);
					}
					finally {
						try {
							if (in != null) {
								in.close();
							}
						}
						catch (final IOException e) {
							LogUtils.warn(e);
						}
					}
				}
			}
		}
		if (redisplay) {
			Controller.getCurrentModeController().getMapController().nodeChanged(target);
		}
		return favoritesFound;
	}
}
