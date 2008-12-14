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
package org.freeplane.addins.mindmapmode.export;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.freeplane.controller.Controller;
import org.freeplane.modes.ModeController;
import org.freeplane.ui.MenuBuilder;

/**
 * @author foltin
 * @author kakeda
 * @author rreppel
 */
public class ExportToImage extends ExportAction {
	public static void createActions(final ModeController modeController) {
		final MenuBuilder menuBuilder = modeController
		    .getUserInputListenerFactory().getMenuBuilder();
		final ExportToImage pngExport = new ExportToImage(
			"accessories/plugins/ExportToImage_PNG.properties_name", 
			"png",
		    "Portable Network Graphic (PNG)");
		pngExport
		    .setTooltip("accessories/plugins/ExportToImage_PNG.properties_documentation");
		modeController.addAction("ExportToImage_PNG", pngExport);
		menuBuilder.addAction("/menu_bar/file/export/export", pngExport,
		    "ExportToImage_PNG", MenuBuilder.AS_CHILD);
		final ExportToImage jpgExport = new ExportToImage(
			"accessories/plugins/ExportToImage_JPEG.properties_name", 
			"jpg",
		    "Compressed image (JPEG)");
		pngExport
		    .setTooltip("accessories/plugins/ExportToImage_JPEG.properties_documentation");
		modeController.addAction("ExportToImage_JPEG", jpgExport);
		menuBuilder.addAction("/menu_bar/file/export/export", jpgExport,
		    "ExportToImage_JPEG", MenuBuilder.AS_CHILD);
	}

	private final String imageDescripton;
	private final String imageType;

	ExportToImage(final String title, final String imageType, final String imageDescripton) {
		super(title);
		this.imageType = imageType;
		this.imageDescripton = imageDescripton;
	}

	public void actionPerformed(final ActionEvent e) {
		final BufferedImage image = createBufferedImage();
		if (image != null) {
			exportToImage(image);
		}
	}

	/**
	 * Export image.
	 */
	public boolean exportToImage(final BufferedImage image) {
		final File chosenFile = chooseFile(imageType, imageDescripton, null);
		if (chosenFile == null) {
			return false;
		}
		try {
			Controller.getController().getViewController().setWaitingCursor(
			    true);
			final FileOutputStream out = new FileOutputStream(chosenFile);
			ImageIO.write(image, imageType, out);
			out.close();
		}
		catch (final IOException e1) {
			org.freeplane.main.Tools.logException(e1);
		}
		Controller.getController().getViewController().setWaitingCursor(false);
		return true;
	}

	public void transForm(final Source xmlSource, final InputStream xsltStream,
	                      final File resultFile, final String areaCode) {
		final Source xsltSource = new StreamSource(xsltStream);
		final Result result = new StreamResult(resultFile);
		try {
			final TransformerFactory transFact = TransformerFactory
			    .newInstance();
			final Transformer trans = transFact.newTransformer(xsltSource);
			trans.setParameter("destination_dir", resultFile.getName()
			        + "_files/");
			trans.setParameter("area_code", areaCode);
			trans.setParameter("folding_type", Controller
			    .getResourceController().getProperty("html_export_folding"));
			trans.transform(xmlSource, result);
		}
		catch (final Exception e) {
			org.freeplane.main.Tools.logException(e);
		};
		return;
	}
}
