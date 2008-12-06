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
package accessories.plugins;

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

import org.freeplane.controller.Freeplane;

import deprecated.freemind.extensions.ExportHook;

/**
 * @author foltin
 * @author kakeda
 * @author rreppel
 */
public class ExportToImage extends ExportHook {
	/**
	 *
	 */
	public ExportToImage() {
		super();
	}

	/**
	 * Export image.
	 */
	public boolean exportToImage(final BufferedImage image, final String type,
	                             final String description) {
		final File chosenFile = chooseFile(type, description, null);
		if (chosenFile == null) {
			return false;
		}
		try {
			Freeplane.getController().getViewController()
			    .setWaitingCursor(true);
			final FileOutputStream out = new FileOutputStream(chosenFile);
			ImageIO.write(image, type, out);
			out.close();
		}
		catch (final IOException e1) {
			org.freeplane.main.Tools.logException(e1);
		}
		Freeplane.getController().getViewController().setWaitingCursor(false);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.MindMapHook#startupMapHook()
	 */
	@Override
	public void startup() {
		super.startup();
		final BufferedImage image = createBufferedImage();
		if (image != null) {
			final String imageType = getResourceString("image_type");
			exportToImage(image, imageType,
			    getResourceString("image_description"));
		}
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
			trans.setParameter("folding_type", Freeplane.getController()
			    .getResourceController().getProperty("html_export_folding"));
			trans.transform(xmlSource, result);
		}
		catch (final Exception e) {
			org.freeplane.main.Tools.logException(e);
		};
		return;
	}
}
