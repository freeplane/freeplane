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

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.Tools;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.features.mindmapmode.text.MTextController;

/** */
class ExportBranchAction extends FreeplaneAction {
	public ExportBranchAction() {
		super("export_branch_new");
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel node = getModeController().getMapController().getSelectedNode();
		if (Controller.getController().getMap() == null || node == null || node.isRoot()) {
			Controller.getController().getViewController().err("Could not export branch.");
			return;
		}
		if (Controller.getController().getMap().getFile() == null) {
			Controller.getController().getViewController().out(
			    "You must save the current map first!");
			((MModeController) getModeController()).save();
		}
		JFileChooser chooser;
		if (Controller.getController().getMap().getFile().getParentFile() != null) {
			chooser = new JFileChooser(Controller.getController().getMap().getFile()
			    .getParentFile());
		}
		else {
			chooser = new JFileChooser();
		}
		if (((MFileManager) UrlManager.getController(getModeController())).getFileFilter() != null) {
			chooser.addChoosableFileFilter(((MFileManager) UrlManager
			    .getController(getModeController())).getFileFilter());
		}
		final int returnVal = chooser.showSaveDialog(Controller.getController().getMapView()
		    .getSelected());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File chosenFile = chooser.getSelectedFile();
			final String ext = UrlManager.getExtension(chosenFile.getName());
			if (!ext
			    .equals(org.freeplane.features.mindmapmode.file.MFileManager.FREEPLANE_FILE_EXTENSION_WITHOUT_DOT)) {
				chosenFile = new File(
				    chosenFile.getParent(),
				    chosenFile.getName()
				            + org.freeplane.features.mindmapmode.file.MFileManager.FREEPLANE_FILE_EXTENSION);
			}
			try {
				UrlManager.fileToUrl(chosenFile);
			}
			catch (final MalformedURLException ex) {
				JOptionPane.showMessageDialog(Controller.getController().getMapView(),
				    "couldn't create valid URL!");
				return;
			}
			if (chosenFile.exists()) {
				final int overwriteMap = JOptionPane.showConfirmDialog(Controller.getController()
				    .getMapView(), getModeController().getText("map_already_exists"), "Freeplane",
				    JOptionPane.YES_NO_OPTION);
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
			final NodeModel parent = node.getParentNode();
			try {
				final String linkToNewMapString = UrlManager.toRelativeURL(UrlManager
				    .fileToUrl(chosenFile), Controller.getController().getMap().getURL());
				((MLinkController) LinkController.getController(Controller.getModeController()))
				    .setLink(node, linkToNewMapString);
			}
			catch (final MalformedURLException ex) {
				Tools.logException(ex);
			}
			final int nodePosition = parent.getChildPosition(node);
			((MMapController) getModeController().getMapController()).deleteNode(node);
			node.setParent(null);
			node.setFolded(false);
			final MapModel map = getModeController().getMapController().newMap(node);
			((MFileManager) UrlManager.getController(getModeController())).save(map, chosenFile);
			final NodeModel newNode = ((MMapController) getModeController().getMapController())
			    .addNewNode(parent, nodePosition, node.isLeft());
			((MTextController) TextController.getController(getModeController())).setNodeText(
			    newNode, node.getText());
			try {
				final String linkString = UrlManager.toRelativeURL(Controller.getController()
				    .getMap().getURL(), UrlManager.fileToUrl(chosenFile));
				((MLinkController) LinkController.getController(Controller.getModeController()))
				    .setLink(newNode, linkString);
			}
			catch (final MalformedURLException ex) {
				Tools.logException(ex);
			}
		}
	}
}
