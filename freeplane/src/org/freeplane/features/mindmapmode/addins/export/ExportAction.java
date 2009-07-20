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
package org.freeplane.features.mindmapmode.addins.export;

import java.awt.Container;
import java.awt.image.RenderedImage;
import java.io.File;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.url.UrlManager;

/**
 * @author foltin
 */
abstract public class ExportAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static File chooseFile(final Controller controller, final String type, final String description,
	                       final String nameExtension) {
		final ModeController mindMapController = controller.getModeController();
		final Container component = controller.getViewController().getContentPane();
		JFileChooser chooser = null;
		chooser = new JFileChooser();
		final File mmFile = controller.getMap().getFile();
		if (mmFile != null) {
			final String proposedName = mmFile.getAbsolutePath().replaceFirst("\\.[^.]*?$", "")
			        + ((nameExtension != null) ? nameExtension : "") + "." + type;
			chooser.setSelectedFile(new File(proposedName));
		}
		final File lastCurrentDir = UrlManager.getController(mindMapController).getLastCurrentDir();
		if (lastCurrentDir != null) {
			chooser.setCurrentDirectory(lastCurrentDir);
		}
		chooser.addChoosableFileFilter(new ExportFilter(type, description));
		final int returnVal = chooser.showSaveDialog(component);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File chosenFile = chooser.getSelectedFile();
		UrlManager.getController(mindMapController).setLastCurrentDir(chosenFile.getParentFile());
		final String ext = UrlManager.getExtension(chosenFile.getName());
		if (!StringUtils.equalsIgnoreCase(ext, type)) {
			chosenFile = new File(chosenFile.getParent(), chosenFile.getName() + "." + type);
		}
		if (chosenFile.exists()) {
			final String overwriteText = MessageFormat.format(ResourceBundles.getText("file_already_exists"),
			    new Object[] { chosenFile.toString() });
			final int overwriteMap = JOptionPane.showConfirmDialog(component, overwriteText, overwriteText,
			    JOptionPane.YES_NO_OPTION);
			if (overwriteMap != JOptionPane.YES_OPTION) {
				return null;
			}
		}
		return chosenFile;
	}

	public ExportAction(final String key, final Controller controller) {
		super(key, controller);
	}

	/**
	 * @param nameExtension
	 */
	protected File chooseFile(final String type, final String description, final String nameExtension) {
		final Controller controller = getController();
		return ExportAction.chooseFile(controller, type, description, nameExtension);
	}

	public RenderedImage createBufferedImage() {
		return getController().getMapViewManager().createImage();
	}
}
