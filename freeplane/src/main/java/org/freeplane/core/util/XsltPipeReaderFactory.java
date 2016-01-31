/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.core.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.n3.nanoxml.XMLParseException;

/**
 * @author Dimitry Polivaev
 * 29.09.2013
 */
public class XsltPipeReaderFactory {

	final private String xsltResource;
	public XsltPipeReaderFactory(final String xsltResource){
		this.xsltResource = xsltResource;

	}

	public Reader getReader(final InputStream in) throws IOException {
		final URL xsltUrl = ResourceController.getResourceController().getResource(xsltResource);
		if (xsltUrl == null) {
			LogUtils.severe("Can't find " + xsltResource + " as resource.");
			throw new IllegalArgumentException("Can't find " + xsltResource + " as resource.");
		}
		final PipedReader reader = new PipedReader();
		final Writer writer = new PipedWriter(reader);
		final Thread transformationThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				InputStream xsltFile = null;
				try{
					xsltFile = xsltUrl.openStream();
					final Result result = new StreamResult(writer);
					transform(new StreamSource(in), xsltFile, result);
				} catch (IOException e) {
					e.printStackTrace();
				}
				finally {
					FileUtils.silentlyClose(xsltFile);
					FileUtils.silentlyClose(writer);
				}
			}
		}, "XSLT Transformation");
		transformationThread.start();
		return reader;
	}

	private void transform(final Source xmlSource, final InputStream xsltStream, final Result result)
			throws TransformerFactoryConfigurationError {
		final Source xsltSource = new StreamSource(xsltStream);
		try {
			final TransformerFactory transFact = TransformerFactory.newInstance();
			final Transformer trans = transFact.newTransformer(xsltSource);
			trans.transform(xmlSource, result);
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
	}
}
