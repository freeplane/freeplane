/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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

import java.awt.event.ActionEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogTool;

/**
 * @author foltin
 * @author kakeda
 * @author rreppel
 */
public class ExportToImage extends ExportAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void createActions(final ModeController modeController) {
		final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
		final ExportToImage pngExport = new ExportToImage(modeController.getController(), "png",
		    "Portable Network Graphic (PNG)");
		modeController.addAction(pngExport);
		menuBuilder.addAction("/menu_bar/file/export", pngExport, MenuBuilder.AS_CHILD);
		final ExportToImage jpgExport = new ExportToImage(modeController.getController(), "jpg",
		    "Compressed image (JPEG)");
		modeController.addAction(jpgExport);
		menuBuilder.addAction("/menu_bar/file/export", jpgExport, MenuBuilder.AS_CHILD);
	}

	private final String imageDescripton;
	private final String imageType;

	ExportToImage(final Controller controller, final String imageType, final String imageDescripton) {
		super("ExportToImage." + imageType, controller);
		this.imageType = imageType;
		this.imageDescripton = imageDescripton;
	}

	public void actionPerformed(final ActionEvent e) {
		try {
			final RenderedImage image = createBufferedImage();
			if (image != null) {
				exportToImage(image);
			}
		}
		catch (final OutOfMemoryError ex) {
			UITools.errorMessage(ResourceBundles.getText("out_of_memory"));
		}
	}

	/**
	 * Export image.
	 */
	public boolean exportToImage(final RenderedImage image) {
		final File chosenFile = chooseFile(imageType, imageDescripton, null);
		if (chosenFile == null) {
			return false;
		}
		try {
			getController().getViewController().setWaitingCursor(true);
			final FileOutputStream out = new FileOutputStream(chosenFile);
			ImageIO.write(image, imageType, out);
			out.close();
		}
		catch (final IOException e1) {
			LogTool.warn(e1);
			UITools.errorMessage(ResourceBundles.getText("export_failed"));
		}
		getController().getViewController().setWaitingCursor(false);
		return true;
	}
}
