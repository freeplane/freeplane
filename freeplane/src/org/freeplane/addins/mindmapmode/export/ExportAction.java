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
package org.freeplane.addins.mindmapmode.export;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeplaneAction;
import org.freeplane.main.Tools;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.modes.ModeController;

/**
 * @author foltin
 */
abstract public class ExportAction extends FreeplaneAction {
	private MapView view;

	public ExportAction() {
	}

	public ExportAction(final String title) {
		super(title);
	}

	/**
	 * @param nameExtension
	 */
	protected File chooseFile(final String type, final String description,
	                          final String nameExtension) {
		final Container component = Controller.getController()
		    .getViewController().getContentPane();
		JFileChooser chooser = null;
		chooser = new JFileChooser();
		final File mmFile = Controller.getController().getMap().getFile();
		if (mmFile != null) {
			final String proposedName = mmFile.getAbsolutePath().replaceFirst(
			    "\\.[^.]*?$", "")
			        + ((nameExtension != null) ? nameExtension : "")
			        + "."
			        + type;
			chooser.setSelectedFile(new File(proposedName));
		}
		final ModeController mindMapController = getModeController();
		final File lastCurrentDir = mindMapController.getUrlManager()
		    .getLastCurrentDir();
		if (lastCurrentDir != null) {
			chooser.setCurrentDirectory(lastCurrentDir);
		}
		chooser.addChoosableFileFilter(new ExportFilter(type, description));
		final int returnVal = chooser.showSaveDialog(component);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File chosenFile = chooser.getSelectedFile();
		mindMapController.getUrlManager().setLastCurrentDir(
		    chosenFile.getParentFile());
		final String ext = Tools.getExtension(chosenFile.getName());
		if (!Tools.safeEqualsIgnoreCase(ext, type)) {
			chosenFile = new File(chosenFile.getParent(), chosenFile.getName()
			        + "." + type);
		}
		if (chosenFile.exists()) {
			final String overwriteText = MessageFormat.format(Controller
			    .getText("file_already_exists"), new Object[] { chosenFile
			    .toString() });
			final int overwriteMap = JOptionPane.showConfirmDialog(component,
			    overwriteText, overwriteText, JOptionPane.YES_NO_OPTION);
			if (overwriteMap != JOptionPane.YES_OPTION) {
				return null;
			}
		}
		return chosenFile;
	}

	/**
	 */
	protected void copyFromFile(final String dir, final String fileName,
	                            final String destinationDirectory) {
		try {
			final File resource = new File(dir, fileName);
			if (resource == null) {
				Logger.global.severe("Cannot find resource: " + dir + fileName);
				return;
			}
			final InputStream in = new FileInputStream(resource);
			final OutputStream out = new FileOutputStream(destinationDirectory
			        + "/" + fileName);
			Tools.copyStream(in, out);
		}
		catch (final Exception e) {
			Logger.global.severe("File not found or could not be copied. "
			        + "Was earching for " + dir + fileName
			        + " and should go to " + destinationDirectory);
		}
	}

	/**
	 */
	protected void copyFromResource(final String prefix, final String fileName,
	                                final String destinationDirectory) {
		try {
			final URL resource = Controller.getResourceController()
			    .getResource(prefix + fileName);
			if (resource == null) {
				Logger.global.severe("Cannot find resource: " + prefix
				        + fileName);
				return;
			}
			final InputStream in = resource.openStream();
			final OutputStream out = new FileOutputStream(destinationDirectory
			        + "/" + fileName);
			Tools.copyStream(in, out);
		}
		catch (final Exception e) {
			Logger.global.severe("File not found or could not be copied. "
			        + "Was earching for " + prefix + fileName
			        + " and should go to " + destinationDirectory);
		}
	}

	public BufferedImage createBufferedImage() {
		view = Controller.getController().getMapView();
		if (view == null) {
			return null;
		}
		view.preparePrinting();
		final Rectangle innerBounds = view.getInnerBounds();
		BufferedImage myImage = (BufferedImage) view.createImage(view
		    .getWidth(), view.getHeight());
		final Graphics g = myImage.getGraphics();
		g.clipRect(innerBounds.x, innerBounds.y, innerBounds.width,
		    innerBounds.height);
		view.print(g);
		myImage = myImage.getSubimage(innerBounds.x, innerBounds.y,
		    innerBounds.width, innerBounds.height);
		return myImage;
	}
}
