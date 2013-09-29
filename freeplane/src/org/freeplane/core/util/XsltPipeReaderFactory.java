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
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.url.CleaningInputStream;

/**
 * @author Dimitry Polivaev
 * 29.09.2013
 */
public class XsltPipeReaderFactory {

	/**
	 * Creates a reader that pipes the input file through a XSLT-Script that
	 * updates the version to the current.
	 *
	 * @throws IOException
	 */
	public Reader getUpdateReader(final File file, final String xsltScript) throws FileNotFoundException,
	        IOException {
		try {
			String updatedXml = transform(file, xsltScript);
			return new StringReader(updatedXml);
		}
		catch (final Exception ex) {
			final String message = ex.getMessage();
			UITools.errorMessage(TextUtils.format("update_failed", String.valueOf(message)));
			LogUtils.warn(ex);
			final InputStream input = new BufferedInputStream(new FileInputStream(file));
			return getActualReader(input);
		}
	}

	public String transform(final File file, final String xsltScript) throws InterruptedException, TransformerException {
	    final URL updaterUrl = ResourceController.getResourceController().getResource(xsltScript);
	    if (updaterUrl == null) {
	    	throw new IllegalArgumentException(xsltScript + " not found.");
	    }
	    final StringWriter writer = new StringWriter();
	    final Result result = new StreamResult(writer);
	    class TransformerRunnable implements Runnable {
	    	private Throwable thrownException = null;

	    	public void run() {
	    		final TransformerFactory transFact = TransformerFactory.newInstance();
	    		InputStream xsltInputStream = null;
	    		InputStream input = null;
	    		try {
	    			xsltInputStream = new BufferedInputStream(updaterUrl.openStream());
	    			final Source xsltSource = new StreamSource(xsltInputStream);
	    			input = new BufferedInputStream(new FileInputStream(file));
	    			final CleaningInputStream cleanedInput = new CleaningInputStream(input);
	    			final Reader reader = new InputStreamReader(cleanedInput, cleanedInput.isUtf8() ? Charset.forName("UTF-8") : FileUtils.defaultCharset());
	    			final Transformer trans = transFact.newTransformer(xsltSource);
	    			trans.transform(new StreamSource(reader), result);
	    		}
	    		catch (final Exception ex) {
	    			LogUtils.warn(ex);
	    			thrownException = ex;
	    		}
	    		finally {
	    			FileUtils.silentlyClose(input, xsltInputStream);
	    		}
	    	}

	    	public Throwable thrownException() {
	    		return thrownException;
	    	}
	    }
	    final TransformerRunnable transformer = new TransformerRunnable();
	    final Thread transformerThread = new Thread(transformer, "XSLT");
	    transformerThread.start();
	    transformerThread.join();
	    final Throwable thrownException = transformer.thrownException();
	    if (thrownException != null) {
	    	throw new TransformerException(thrownException);
	    }
	    String updatedXml = writer.getBuffer().toString();
	    return updatedXml;
    }

	/**
	 * Creates a default reader that just reads the given file.
	 *
	 * @throws FileNotFoundException
	 */
	public Reader getActualReader(final InputStream file) throws FileNotFoundException {
		return new InputStreamReader(file, FileUtils.defaultCharset());
	}
}
