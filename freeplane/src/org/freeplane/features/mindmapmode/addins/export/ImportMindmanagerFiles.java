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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JFileChooser;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogTool;
import org.freeplane.n3.nanoxml.XMLParseException;

/**
 * Applies an XSLT to the Document.xml file of MindManager(c) files.
 */
@ActionLocationDescriptor(locations = { "/menu_bar/file/import/import" })
public class ImportMindmanagerFiles extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportMindmanagerFiles(final Controller controller) {
		super("ImportMindmanagerFiles", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final String type = "mmap";
		final Container component = getController().getViewController().getContentPane();
		final JFileChooser chooser = new JFileChooser();
		chooser.addChoosableFileFilter(new ExportFilter(type, null));
		final File mmFile = getController().getMap().getFile();
		if (mmFile != null && mmFile.getParentFile() != null) {
			chooser.setSelectedFile(mmFile.getParentFile());
		}
		final int returnVal = chooser.showOpenDialog(component);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		final File chosenFile = chooser.getSelectedFile();
		importMindmanagerFile(chosenFile);
	}

	private void importMindmanagerFile(final File file) {
		try {
			final ZipInputStream in = new ZipInputStream(new FileInputStream(file));
			while (in.available() != 0) {
				final ZipEntry entry = in.getNextEntry();
				if(entry == null){
					break;
				}
				if (!entry.getName().equals("Document.xml")) {
					continue;
				}
				final String xsltFileName = "/xslt/mindmanager2mm.xsl";
				final URL xsltUrl = ResourceController.getResourceController().getResource(xsltFileName);
				if (xsltUrl == null) {
					LogTool.severe("Can't find " + xsltFileName + " as resource.");
					throw new IllegalArgumentException("Can't find " + xsltFileName + " as resource.");
				}
				final InputStream xsltFile = xsltUrl.openStream();
				final String xml = transForm(new StreamSource(in), xsltFile);
				if (xml != null) {
					final File tempFile = File.createTempFile(file.getName(),
					    org.freeplane.core.url.UrlManager.FREEPLANE_FILE_EXTENSION, file.getParentFile());
					final FileWriter fw = new FileWriter(tempFile);
					fw.write(xml);
					fw.close();
					getModeController().getMapController().newMap(Compat.fileToUrl(tempFile));
				}
				break;
			}
		}
		catch (final IOException e) {
			LogTool.severe(e);
		}
		catch (final XMLParseException e) {
			LogTool.severe(e);
		}
		catch (final URISyntaxException e) {
			LogTool.severe(e);
		}
	}

	public String transForm(final Source xmlSource, final InputStream xsltStream) {
		final Source xsltSource = new StreamSource(xsltStream);
		final StringWriter writer = new StringWriter();
		final Result result = new StreamResult(writer);
		try {
			final TransformerFactory transFact = TransformerFactory.newInstance();
			final Transformer trans = transFact.newTransformer(xsltSource);
			trans.transform(xmlSource, result);
		}
		catch (final Exception e) {
			LogTool.severe(e);
			return null;
		}
		return writer.toString();
	}
}
