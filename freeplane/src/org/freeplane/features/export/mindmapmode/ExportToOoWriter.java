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
package org.freeplane.features.export.mindmapmode;

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

import javax.swing.filechooser.FileFilter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ExampleFileFilter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

/**
 * @author foltin
 */
public class ExportToOoWriter implements IExportEngine {
	public ExportToOoWriter() {
	}

 	
	public FileFilter getFileFilter(){
		return new ExampleFileFilter("odt", TextUtils.getText("ExportToOoWriter.text"));
	}
	
	public void export(MapModel map, File chosenFile) {
			Controller.getCurrentController().getViewController().setWaitingCursor(true);
		try {
			exportToOoWriter(map, chosenFile);
		}
		catch (final Exception ex) {
			LogUtils.warn(ex);
			UITools.errorMessage(TextUtils.getText("export_failed"));
		}
		finally{
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
	}

	/**
	 * @return true, if successful.
	 */
	private void applyXsltFile(final String xsltFileName, final StringWriter writer, final Result result)
	        throws IOException {
		final URL xsltUrl = ResourceController.getResourceController().getResource(xsltFileName);
		if (xsltUrl == null) {
			LogUtils.severe("Can't find " + xsltFileName + " as resource.");
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
			LogUtils.warn(e);
			return;
		}
		finally {
			FileUtils.silentlyClose(xsltStream);
		}
	}


	public void exportToOoWriter(MapModel map, final File file) throws IOException {
		final ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(file));
		try {
			final StringWriter writer = new StringWriter();
			final ModeController controller = Controller.getCurrentModeController();
			controller.getMapController().getFilteredXml(map, writer, Mode.EXPORT, true);
			final Result result = new StreamResult(zipout);

			ZipEntry entry = new ZipEntry("content.xml");
			zipout.putNextEntry(entry);
			applyXsltFile("/xslt/export2oowriter.xsl", writer, result);
			zipout.closeEntry();

			entry = new ZipEntry("META-INF/manifest.xml");
			zipout.putNextEntry(entry);
			applyXsltFile("/xslt/export2oowriter.manifest.xsl", writer, result);
			zipout.closeEntry();

			entry = new ZipEntry("styles.xml");
			zipout.putNextEntry(entry);
			applyXsltFile("/xslt/export2oowriter.styles.xsl", writer, result);
			zipout.closeEntry();
		}
		finally {
			zipout.close();
		}
	}
}
