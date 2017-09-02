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
import java.net.URI;
import java.util.Collection;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.MapExtensions;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.styles.LogicalStyleKeys;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.url.UrlManager;

class ExportBranchAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExportBranchAction() {
		super("ExportBranchAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel existingNode = Controller.getCurrentModeController().getMapController().getSelectedNode();
		final Controller controller = Controller.getCurrentController();
		final MapModel parentMap = controller.getMap();
		if (parentMap == null || existingNode == null || existingNode.isRoot()) {
			controller.getViewController().err("Could not export branch.");
			return;
		}
		if (parentMap.getFile() == null) {
			controller.getViewController().out("You must save the current map first!");
			((MModeController) Controller.getCurrentModeController()).save();
		}
		JFileChooser chooser;
		final File file = parentMap.getFile();
		if (file == null) {
			return;
		}
		chooser = new JFileChooser(file.getParentFile());
		chooser.setSelectedFile(new File(createFileName(TextController.getController().getShortPlainText(existingNode))));
		if (((MFileManager) UrlManager.getController()).getFileFilter() != null) {
			chooser.addChoosableFileFilter(((MFileManager) UrlManager.getController())
			    .getFileFilter());
		}
		final int returnVal = chooser.showSaveDialog(controller.getViewController().getCurrentRootComponent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File chosenFile = chooser.getSelectedFile();
			final String ext = FileUtils.getExtension(chosenFile.getName());
			if (!ext.equals(org.freeplane.features.url.UrlManager.FREEPLANE_FILE_EXTENSION_WITHOUT_DOT)) {
				chosenFile = new File(chosenFile.getParent(), chosenFile.getName()
				        + org.freeplane.features.url.UrlManager.FREEPLANE_FILE_EXTENSION);
			}
			try {
				Compat.fileToUrl(chosenFile);
			}
			catch (final MalformedURLException ex) {
				UITools.errorMessage(TextUtils.getText("invalid_url"));
				return;
			}
			if (chosenFile.exists()) {
				final int overwriteMap = JOptionPane.showConfirmDialog(controller.getMapViewManager().getMapViewComponent(),
				    TextUtils.getText("map_already_exists"), "Freeplane", JOptionPane.YES_NO_OPTION);
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
			final File oldFile = parentMap.getFile();
	
			final URI newUri = LinkController.toLinkTypeDependantURI(oldFile, chosenFile);
			final URI oldUri = LinkController.toLinkTypeDependantURI(chosenFile, file);
			((MLinkController) LinkController.getController()).setLink(existingNode,
			    oldUri, LinkController.LINK_ABSOLUTE);
			final int nodePosition = parent.getIndex(existingNode);
			final ModeController modeController = Controller.getCurrentModeController();
			modeController.undoableResolveParentExtensions(LogicalStyleKeys.NODE_STYLE, existingNode);
			final MMapController mMapController = (MMapController) modeController.getMapController();
			mMapController.deleteNode(existingNode);
			{
				final IActor actor = new IActor() {
					private final boolean wasFolded = existingNode.isFolded();

					public void undo() {
						PersistentNodeHook.removeMapExtensions(existingNode);
						existingNode.setMap(parentMap);
						existingNode.setFolded(wasFolded);
					}

					public String getDescription() {
						return "ExportBranchAction";
					}

					public void act() {
						existingNode.setFolded(false);
					}
				};
				Controller.getCurrentModeController().execute(actor, parentMap);
			}
			mMapController.newModel(existingNode);
			final MapModel newMap = existingNode.getMap();
			IExtension[] oldExtensions = newMap.getRootNode().getSharedExtensions().values().toArray(new IExtension[]{});
			for(final IExtension extension : oldExtensions){
				final Class<? extends IExtension> clazz = extension.getClass();
				if(MapExtensions.isMapExtension(clazz)){
					existingNode.removeExtension(clazz);
				}
			}
			final Collection<IExtension> newExtensions = parentMap.getRootNode().getSharedExtensions().values();
			for(final IExtension extension : newExtensions){
				final Class<? extends IExtension> clazz = extension.getClass();
				if(MapExtensions.isMapExtension(clazz)){
					existingNode.addExtension(extension);
				}
			}
			((MFileManager) UrlManager.getController()).save(newMap, chosenFile);
			final NodeModel newNode = mMapController.addNewNode(parent, nodePosition, existingNode.isLeft());
			((MTextController) TextController.getController()).setNodeText(newNode, existingNode.getText());
			modeController.undoableCopyExtensions(LogicalStyleKeys.NODE_STYLE, existingNode, newNode);
			newMap.getFile();
			((MLinkController) LinkController.getController()).setLink(newNode, newUri, LinkController.LINK_ABSOLUTE);
			newMap.destroy();
			existingNode.setParent(null);
			mMapController.select(newNode);
		}
	}

	private String createFileName(final String shortText) {
		final StringBuilder builder = new StringBuilder(50);
		final String[] words = shortText.split("\\s");
		for (final String word : words) {
			if ("...".equals(word)) {
				continue;
			}
			builder.append(StringUtils.capitalize(word));
		}
		return builder.toString();
	}
}
