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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.io.MapWriter.Mode;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogTool;

/**
 * @author foltin
 */
@ActionLocationDescriptor(locations = { "/menu_bar/file/export" })
public class ExportToOoWriter extends ExportAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExportToOoWriter(final Controller controller) {
		super("ExportToOoWriter", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final File chosenFile = chooseFile("odt", null, null);
		if (chosenFile == null) {
			return;
		}
		getController().getViewController().setWaitingCursor(true);
		try {
			exportToOoWriter(chosenFile);
		}
		catch (final Exception ex) {
			LogTool.warn(ex);
			UITools.errorMessage(ResourceBundles.getText("export_failed"));
		}
		getController().getViewController().setWaitingCursor(false);
	}

	/**
	 * @return true, if successful.
	 */
	private void applyXsltFile(final String xsltFileName, final StringWriter writer, final Result result)
	        throws IOException {
		final URL xsltUrl = ResourceController.getResourceController().getResource(xsltFileName);
		if (xsltUrl == null) {
			LogTool.severe("Can't find " + xsltFileName + " as resource.");
			throw new IllegalArgumentException("Can't find " + xsltFileName + " as resource.");
		}
		final InputStream xsltStream = new BufferedInputStream(xsltUrl.openStream());
		final Source xsltSource = new StreamSource(xsltStream);
		try {
			final StringReader reader = new StringReader(writer.getBuffer().toString());
			final TransformerFactory transFact = TransformerFactory.newInstance();
			final Transformer trans = transFact.newTransformer(xsltSource);
			trans.transform(new StreamSource(reader), result);
			return;
		}
		catch (final Exception e) {
			UITools.errorMessage(e.getMessage());
			LogTool.warn(e);
			return;
		}
	}

	/**
	 * @return true, if successful.
	 */
	private void copyFromResource(final String fileName, final OutputStream out) {
		try {
			final URL resource = ResourceController.getResourceController().getResource(fileName);
			if (resource == null) {
				LogTool.severe("Cannot find resource: " + fileName);
				return;
			}
			final InputStream in = resource.openStream();
			final byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			return;
		}
		catch (final Exception e) {
			LogTool.severe("File not found or could not be copied. " + "Was earching for " + fileName
			        + " and should go to " + out);
			LogTool.severe(e);
			return;
		}
	}

	public void exportToOoWriter(final File file) throws IOException {
		final ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(file));
		try {
			final StringWriter writer = new StringWriter();
			final ModeController controller = getModeController();
			final MapModel map = controller.getController().getMap();
			controller.getMapController().getFilteredXml(map, writer, Mode.EXPORT);
			final Result result = new StreamResult(zipout);
			ZipEntry entry = new ZipEntry("content.xml");
			zipout.putNextEntry(entry);
			applyXsltFile("/xslt/mm2oowriter.xsl", writer, result);
			zipout.closeEntry();
			entry = new ZipEntry("META-INF/manifest.xml");
			zipout.putNextEntry(entry);
			applyXsltFile("/xslt/mm2oowriter.manifest.xsl", writer, result);
			zipout.closeEntry();
			entry = new ZipEntry("styles.xml");
			zipout.putNextEntry(entry);
			copyFromResource("/xml/mm2oowriterStyles.xml", zipout);
			zipout.closeEntry();
		}
		finally {
			zipout.close();
		}
	}

	public void transForm(final Source xmlSource, final InputStream xsltStream, final File resultFile,
	                      final String areaCode) {
		final Source xsltSource = new StreamSource(xsltStream);
		final Result result = new StreamResult(resultFile);
		try {
			final TransformerFactory transFact = TransformerFactory.newInstance();
			final Transformer trans = transFact.newTransformer(xsltSource);
			trans.setParameter("destination_dir", resultFile.getName() + "_files/");
			trans.setParameter("area_code", areaCode);
			trans.setParameter("folding_type", ResourceController.getResourceController().getProperty(
			    "html_export_folding"));
			trans.transform(xmlSource, result);
		}
		catch (final Exception e) {
			LogTool.severe(e);
		};
		return;
	}
}
