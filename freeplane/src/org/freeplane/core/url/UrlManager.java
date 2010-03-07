/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.core.url;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.AccessControlException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.MapWriter.Mode;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogTool;
import org.freeplane.n3.nanoxml.XMLParseException;

/**
 * @author Dimitry Polivaev
 */
public class UrlManager implements IExtension {
	public static final String FREEPLANE_FILE_EXTENSION_WITHOUT_DOT = "mm";
	public static final String FREEPLANE_FILE_EXTENSION = "." + FREEPLANE_FILE_EXTENSION_WITHOUT_DOT;
	private static File lastCurrentDir = null;
	public static final String MAP_URL = "map_url";

	/**
	 * Creates a default reader that just reads the given file.
	 *
	 * @throws FileNotFoundException
	 */
	protected static Reader getActualReader(final InputStream file) throws FileNotFoundException {
			return new InputStreamReader(file, defaultCharset());
	}

	protected static Charset defaultCharset() {
		try {
			String defaultCharsetName = ResourceController.getResourceController().getProperty("default_charset");
			if(defaultCharsetName.equals("JVMdefault")){
				return Charset.defaultCharset();
			}
			return Charset.forName(defaultCharsetName);
		} catch (Exception e) {
			return Charset.defaultCharset();
		}
	}

	public static UrlManager getController(final ModeController modeController) {
		return (UrlManager) modeController.getExtension(UrlManager.class);
	}

	/**
	 * Returns the lowercase of the extension of a file.
	 */
	public static String getExtension(final File f) {
		return UrlManager.getExtension(f.toString());
	}

	/**
	 * Returns the lowercase of the extension of a file.
	 */
	public static String getExtension(final String s) {
		if(s == null){
			return null;
		}
		final int i = s.lastIndexOf('.');
		return (i > 0 && i < s.length() - 1) ? s.substring(i + 1).toLowerCase().trim() : "";
	}

	/**
	 * Creates a reader that pipes the input file through a XSLT-Script that
	 * updates the version to the current.
	 *
	 * @throws IOException
	 */
	public static Reader getUpdateReader(final File file, final String xsltScript) throws FileNotFoundException,
	        IOException {
        try {
        	final URL updaterUrl = ResourceController.getResourceController().getResource(xsltScript);
        	if (updaterUrl == null) {
        		throw new IllegalArgumentException(xsltScript + " not found.");
        	}
        	StringWriter writer = new StringWriter();
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
        				input = new BufferedInputStream( new FileInputStream(file));
                        InputStream cleanedInput = new CleaningInputStream(input);
                        Reader reader = new InputStreamReader(cleanedInput, defaultCharset());
        				Transformer trans = transFact.newTransformer(xsltSource);
        				trans.transform(new StreamSource(reader), result);
        			}
        			catch (final Exception ex) {
        				LogTool.warn(ex);
        				thrownException = ex;
        			}
        			finally{
        				try {
	                        if(input != null) input.close();
	                        if(xsltInputStream != null) xsltInputStream.close();
                        }
                        catch (IOException e) {
	                        e.printStackTrace();
                        }
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
        	return new StringReader(writer.getBuffer().toString());
        }
        catch (final Exception ex) {
			final String message = ex.getMessage();
			UITools.errorMessage(FpStringUtils.formatText("update_failed", String.valueOf(message)));
        	LogTool.warn(ex);
			final InputStream input = new BufferedInputStream( new FileInputStream(file));
        	return UrlManager.getActualReader(input);
        }
 	}
	public static void install(final ModeController modeController, final UrlManager urlManager) {
		modeController.addExtension(UrlManager.class, urlManager);
	}

	public static boolean isAbsolutePath(final String path) {
		final String osNameStart = System.getProperty("os.name").substring(0, 3);
		final String fileSeparator = System.getProperty("file.separator");
		if (osNameStart.equals("Win")) {
			return ((path.length() > 1) && path.substring(1, 2).equals(":")) || path.startsWith(fileSeparator);
		}
		else if (osNameStart.equals("Mac")) {
			return path.startsWith(fileSeparator);
		}
		else {
			return path.startsWith(fileSeparator);
		}
	}

	/**
	 * In case of trouble, the method returns null.
	 *
	 * @param pInputFile
	 *            the file to read.
	 * @return the complete content of the file. or null if an exception has
	 *         occured.
	 */
	public static String readFile(final File pInputFile) {
		final StringBuilder lines = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(pInputFile));
			final String endLine = System.getProperty("line.separator");
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				lines.append(line).append(endLine);
			}
			bufferedReader.close();
		}
		catch (final Exception e) {
			LogTool.severe(e);
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				}
				catch (final Exception ex) {
					LogTool.severe(ex);
				}
			}
			return null;
		}
		return lines.toString();
	}

	public static String removeExtension(final String s) {
		final int i = s.lastIndexOf('.');
		return (i > 0 && i < s.length() - 1) ? s.substring(0, i) : s;
	}

	public static void setHidden(final File file, final boolean hidden, final boolean synchronously) {
		final String osNameStart = System.getProperty("os.name").substring(0, 3);
		if (osNameStart.equals("Win")) {
			try {
				Controller.exec("attrib " + (hidden ? "+" : "-") + "H \"" + file.getAbsolutePath() + "\"");
				if (!synchronously) {
					return;
				}
				int timeOut = 10;
				while (file.isHidden() != hidden && timeOut > 0) {
					Thread.sleep(10/* miliseconds */);
					timeOut--;
				}
			}
			catch (final Exception e) {
				LogTool.severe(e);
			}
		}
	}

	final private Controller controller;
	final private ModeController modeController;

	public UrlManager(final ModeController modeController) {
		super();
		this.modeController = modeController;
		controller = modeController.getController();
		createActions();
	}

	/**
	 *
	 */
	private void createActions() {
	}

	public Controller getController() {
		return controller;
	}

	/**
	 * Creates a file chooser with the last selected directory as default.
	 */
	public JFileChooser getFileChooser(final FileFilter filter) {
		final JFileChooser chooser = new JFileChooser();
		final File parentFile = getMapsParentFile();
		if (parentFile != null && getLastCurrentDir() == null) {
			setLastCurrentDir(parentFile);
		}
		if (getLastCurrentDir() != null) {
			chooser.setCurrentDirectory(getLastCurrentDir());
		}
		if (filter != null) {
			chooser.addChoosableFileFilter(filter);
			chooser.setFileFilter(filter);
		}
		return chooser;
	}

	public File getLastCurrentDir() {
		return lastCurrentDir;
	}

	protected File getMapsParentFile() {
		final MapModel map = getController().getMap();
		if ((map != null) && (map.getFile() != null) && (map.getFile().getParentFile() != null)) {
			return map.getFile().getParentFile();
		}
		return null;
	}

	public ModeController getModeController() {
		return modeController;
	}

	public void handleLoadingException(final Exception ex) {
		final String exceptionType = ex.getClass().getName();
		if (exceptionType.equals("freeplane.main.XMLParseException")) {
			final int showDetail = JOptionPane.showConfirmDialog(getController().getViewController().getMapView(),
			    ResourceBundles.getText("map_corrupted"), "Freeplane", JOptionPane.YES_NO_OPTION,
			    JOptionPane.ERROR_MESSAGE);
			if (showDetail == JOptionPane.YES_OPTION) {
				UITools.errorMessage(ex);
			}
		}
		else if (exceptionType.equals("java.io.FileNotFoundException")) {
			UITools.errorMessage(ex.getMessage());
		}
		else {
			LogTool.severe(ex);
			UITools.errorMessage(ex);
		}
	}

	public void load(final URL url, final MapModel map) throws FileNotFoundException, IOException, XMLParseException,
	        URISyntaxException {
		setURL(map, url);
		InputStreamReader urlStreamReader = null;
		try {
			urlStreamReader = new InputStreamReader(url.openStream());
		}
		catch (final AccessControlException ex) {
			UITools.errorMessage("Could not open URL " + url + ". Access Denied.");
			LogTool.warn(ex.getMessage());
			return;
		}
		catch (final Exception ex) {
			UITools.errorMessage("Could not open URL " + url + ".");
			LogTool.warn(ex.getMessage());
			return;
		}
		try {
			final NodeModel root = modeController.getMapController().getMapReader().createNodeTreeFromXml(map,
			    urlStreamReader, Mode.FILE);
			urlStreamReader.close();
			if (root != null) {
				map.setRoot(root);
			}
			else {
				throw new IOException();
			}
		}
		catch (final Exception ex) {
			LogTool.severe(ex);
			return;
		}
	}

	public void loadURL(URI uri) {
		final String uriString = uri.toString();
		if (uriString.startsWith("#")) {
			final String target = uri.getFragment();
			try {
				final MapController mapController = modeController.getMapController();
				final NodeModel node = mapController.getNodeFromID(target);
				if(node != null){
					mapController.select(node);
				}
			}
			catch (final Exception e) {
				LogTool.warn("link " + target + " not found", e);
				UITools.errorMessage(FpStringUtils.formatText("link_not_found", target));
			}
			return;
		}
		try {
			final String extension = UrlManager.getExtension(uri.getRawPath());
			uri = getAbsoluteUri(uri);
			try {
				if ((extension != null)
						&& extension.equals(org.freeplane.core.url.UrlManager.FREEPLANE_FILE_EXTENSION_WITHOUT_DOT)) {
					URL	url = new URL(uri.getScheme(), uri.getHost(), uri.getPath());
					modeController.getMapController().newMap(url);
					String ref = uri.getFragment();
					if (ref != null) {
						final ModeController newModeController = getController().getModeController();
						final MapController newMapController = newModeController.getMapController();
						newMapController.select(newMapController.getNodeFromID(ref));
					}
					return;
				}
				getController().getViewController().openDocument(uri);
			}
			catch (Exception e) {
				LogTool.warn("link " + uri + " not found", e);
				UITools.errorMessage(FpStringUtils.formatText("link_not_found", uri.toString()));
			}
			return;
		}
		catch (final MalformedURLException ex) {
			LogTool.warn("URL " + uriString + " not found", ex);
			UITools.errorMessage(FpStringUtils.formatText("link_not_found", uriString));
		}
	}

	private URI getAbsoluteUri(URI uri) throws MalformedURLException {
		if(uri.isAbsolute()){
			return uri;
		}
		final MapModel map = getController().getMap();
		return getAbsoluteUri(map, uri);
   }

	public URI getAbsoluteUri(final MapModel map, final URI uri) throws MalformedURLException {
		if (uri.isAbsolute()) {
			return uri;
		}
		final String path = uri.getPath();
		URL url = new URL(map.getURL(), path);
		try {
			return new URI(url.getProtocol(), url.getHost(), url.getPath(), uri.getQuery(), uri.getFragment());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	public URL getAbsoluteUrl(final MapModel map, final URI uri) throws MalformedURLException {
		final String path = uri.isOpaque() ? uri.getSchemeSpecificPart() : uri.getPath();
		StringBuilder sb = new StringBuilder(path);
		final String query = uri.getQuery();
		if(query != null){
			sb.append('?');
			sb.append(query);
		}
		final String fragment = uri.getFragment();
		if(fragment != null){
			sb.append('#');
			sb.append(fragment);
		}
		if (!uri.isAbsolute() || uri.isOpaque()) {
			URL mapUrl = map.getURL();
			String scheme = uri.getScheme();
			if(scheme == null || mapUrl.getProtocol().equals(scheme)){
				final URL url = new URL(mapUrl, sb.toString());
				return url;
			}
		}
		final URL url = new URL(uri.getScheme(), uri.getHost(), uri.getPort(), sb.toString());
		return url;
    }

	public void setLastCurrentDir(final File lastCurrentDir) {
		UrlManager.lastCurrentDir = lastCurrentDir;
	}

	protected void setURL(final MapModel map, final URL url) {
		map.setURL(url);
	}

	public void startup() {
	}
}
