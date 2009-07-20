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
import java.net.URI;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.Compat;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.features.mindmapmode.text.MTextController;

class ExportBranchAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExportBranchAction(final Controller controller) {
		super("ExportBranchAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel existingNode = getModeController().getMapController().getSelectedNode();
		final Controller controller = getController();
		if (controller.getMap() == null || existingNode == null || existingNode.isRoot()) {
			controller.getViewController().err("Could not export branch.");
			return;
		}
		if (controller.getMap().getFile() == null) {
			controller.getViewController().out("You must save the current map first!");
			((MModeController) getModeController()).save();
		}
		JFileChooser chooser;
		final File file = controller.getMap().getFile();
		if (file == null) {
			return;
		}
		chooser = new JFileChooser(file.getParentFile());
		if (((MFileManager) UrlManager.getController(getModeController())).getFileFilter() != null) {
			chooser.addChoosableFileFilter(((MFileManager) UrlManager.getController(getModeController()))
			    .getFileFilter());
		}
		final int returnVal = chooser.showSaveDialog(controller.getViewController().getContentPane());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File chosenFile = chooser.getSelectedFile();
			final String ext = UrlManager.getExtension(chosenFile.getName());
			if (!ext.equals(org.freeplane.core.url.UrlManager.FREEPLANE_FILE_EXTENSION_WITHOUT_DOT)) {
				chosenFile = new File(chosenFile.getParent(), chosenFile.getName()
				        + org.freeplane.core.url.UrlManager.FREEPLANE_FILE_EXTENSION);
			}
			try {
				Compat.fileToUrl(chosenFile);
			}
			catch (final MalformedURLException ex) {
				UITools.errorMessage("couldn't create valid URL!");
				return;
			}
			if (chosenFile.exists()) {
				final int overwriteMap = JOptionPane.showConfirmDialog(controller.getViewController().getMapView(),
				    ResourceBundles.getText("map_already_exists"), "Freeplane", JOptionPane.YES_NO_OPTION);
				if (overwriteMap != JOptionPane.YES_OPTION) {
					return;
				}
			}
			/*
			 * Now make a copy from the node, remove the node from the map and
			 * create a new Map with the node as root, store the new Map, add
			 * the copy of the node to the parent, and set a link from the copy
			 * to the new Map.
			 */
			final NodeModel parent = existingNode.getParentNode();
			final URL oldUrl = existingNode.getMap().getURL();
			final URI newUri = UrlManager.toRelativeURI(oldUrl, chosenFile);
			final int nodePosition = parent.getChildPosition(existingNode);
			((MMapController) getModeController().getMapController()).deleteNode(existingNode);
			existingNode.setParent(null);
			existingNode.setFolded(false);
			final MapModel map = getModeController().getMapController().newMap(existingNode);
			((MFileManager) UrlManager.getController(getModeController())).save(map, chosenFile);
			getController().getMapViewManager().updateMapViewName();
			final NodeModel newNode = ((MMapController) getModeController().getMapController()).addNewNode(parent,
			    nodePosition, existingNode.isLeft());
			((MTextController) TextController.getController(getModeController())).setNodeText(newNode, existingNode
			    .getText());
			final URL newUrl = map.getURL();
			final URI oldUri = UrlManager.toRelativeURI(newUrl, file);
			((MLinkController) LinkController.getController(controller.getModeController())).setLink(newNode, newUri);
			((MLinkController) LinkController.getController(controller.getModeController())).setLink(existingNode,
			    oldUri);
		}
	}
}
