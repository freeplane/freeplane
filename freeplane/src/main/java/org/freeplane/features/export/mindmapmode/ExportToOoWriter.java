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

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.CaseSensitiveFileNameExtensionFilter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import javax.swing.filechooser.FileFilter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author foltin
 */
public class ExportToOoWriter implements IExportEngine {
	public ExportToOoWriter() {
	}

 	
	public FileFilter getFileFilter(){
		return new CaseSensitiveFileNameExtensionFilter("odt", TextUtils.getText("ExportToOoWriter.text"));
	}
	
	public void export(List<NodeModel> branches, File chosenFile) {
			Controller.getCurrentController().getViewController().setWaitingCursor(true);
		try {
			exportToOoWriter(branches, chosenFile);
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
	private void applyXsltFile(final String xsltFileName, final StringWriter writer, final Result result) {
		final URL xsltUrl = ResourceController.getResourceController().getResource(xsltFileName);
		if (xsltUrl == null) {
			LogUtils.severe("Can't find " + xsltFileName + " as resource.");
			throw new IllegalArgumentException("Can't find " + xsltFileName + " as resource.");
		}
		try (InputStream xsltStream = new BufferedInputStream(xsltUrl.openStream())){
			final StringReader reader = new StringReader(writer.getBuffer().toString());
			final TransformerFactory transFact = TransformerFactory.newInstance();
			final Source xsltSource = new StreamSource(xsltStream);
			final Transformer trans = transFact.newTransformer(xsltSource);
			trans.transform(new StreamSource(reader), result);
			return;
		}
		catch (final Exception e) {
			UITools.errorMessage(e.getMessage());
			LogUtils.warn(e);
			return;
		}
	}


	public void exportToOoWriter(List<NodeModel> branches, final File file) throws IOException {
		try (final ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(file));){
			final StringWriter writer = new StringWriter();
			new BranchXmlWriter(branches).writeXml(writer, Mode.EXPORT);
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
	}
}
