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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.MapWriter.Mode;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogTool;

/**
 * @author Dimitry Polivaev
 */
public class UrlManager implements IExtension {
	public static final String FREEPLANE_FILE_EXTENSION_WITHOUT_DOT = "mm";
	public static final String FREEPLANE_FILE_EXTENSION = "." + FREEPLANE_FILE_EXTENSION_WITHOUT_DOT;
	private static File lastCurrentDir = null;

	/**
	 * Creates a default reader that just reads the given file.
	 *
	 * @throws FileNotFoundException
	 */
	public static Reader getActualReader(final File file) throws FileNotFoundException {
		return new BufferedReader(new FileReader(file));
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
		final int i = s.lastIndexOf('.');
		return (i > 0 && i < s.length() - 1) ? s.substring(i + 1).toLowerCase().trim() : "";
	}

	/**
	 * In case of trouble, the method returns null.
	 *
	 * @param pInputFile
	 *            the file to read.
	 * @return the complete content of the file. or null if an exception has
	 *         occured.
	 */
	public static String getFile(final File pInputFile) {
		final StringBuffer lines = new StringBuffer();
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

	/**
	 * Creates a reader that pipes the input file through a XSLT-Script that
	 * updates the version to the current.
	 *
	 * @throws IOException
	 */
	public static Reader getUpdateReader(final File file, final String xsltScript) throws IOException {
		StringWriter writer = null;
		InputStream inputStream = null;
		boolean successful = false;
		try {
			URL updaterUrl = null;
			updaterUrl = ResourceController.getResourceController().getResource(xsltScript);
			if (updaterUrl == null) {
				throw new IllegalArgumentException(xsltScript + " not found.");
			}
			inputStream = updaterUrl.openStream();
			final Source xsltSource = new StreamSource(inputStream);
			writer = new StringWriter();
			final Result result = new StreamResult(writer);
			class TransformerRunnable implements Runnable {
				private boolean successful = false;

				public boolean isSuccessful() {
					return successful;
				}

				public void run() {
					final TransformerFactory transFact = TransformerFactory.newInstance();
					Transformer trans;
					try {
						trans = transFact.newTransformer(xsltSource);
						trans.transform(new StreamSource(file), result);
						successful = true;
					}
					catch (final Exception ex) {
						LogTool.severe(ex);
					}
				}
			}
			final TransformerRunnable transformer = new TransformerRunnable();
			final Thread transformerThread = new Thread(transformer, "XSLT");
			transformerThread.start();
			transformerThread.join();
			successful = transformer.isSuccessful();
		}
		catch (final Exception ex) {
		}
		finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (writer != null) {
				writer.close();
			}
		}
		if (successful) {
			return new StringReader(writer.getBuffer().toString());
		}
		else {
			return UrlManager.getActualReader(file);
		}
	}

	/**
	 * Returns the same URL as input with the addition, that the reference part
	 * "#..." is filtered out.
	 *
	 * @throws MalformedURLException
	 */
	public static URL getURLWithoutReference(final URL input) throws MalformedURLException {
		return new URL(input.toString().replaceFirst("#.*", ""));
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

	public static String removeExtension(final String s) {
		final int i = s.lastIndexOf('.');
		return (i > 0 && i < s.length() - 1) ? s.substring(0, i) : "";
	}

	public static void setHidden(final File file, final boolean hidden, final boolean synchronously) {
		final String osNameStart = System.getProperty("os.name").substring(0, 3);
		if (osNameStart.equals("Win")) {
			try {
				Runtime.getRuntime().exec("attrib " + (hidden ? "+" : "-") + "H \"" + file.getAbsolutePath() + "\"");
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

	/**
	 * This method converts an absolute url to an url relative to a given
	 * base-url. The algorithm is somewhat chaotic, but it works (Maybe rewrite
	 * it). Be careful, the method is ".mm"-specific. Something like this should
	 * be included in the librarys, but I couldn't find it. You can create a new
	 * absolute url with "new URL(URL context, URL relative)".
	 */
	public static String toRelativeURL(final URL base, final URL target) {
		if ((base.getProtocol().equals(target.getProtocol())) && (base.getHost().equals(target.getHost()))) {
			String baseString = base.getFile();
			String targetString = target.getFile();
			String result = "";
			baseString = baseString.substring(0, baseString.lastIndexOf("/") + 1);
			targetString = targetString.substring(0, targetString.lastIndexOf("/") + 1);
			final StringTokenizer baseTokens = new StringTokenizer(baseString, "/");
			final StringTokenizer targetTokens = new StringTokenizer(targetString, "/");
			String nextBaseToken = "", nextTargetToken = "";
			while (baseTokens.hasMoreTokens() && targetTokens.hasMoreTokens()) {
				nextBaseToken = baseTokens.nextToken();
				nextTargetToken = targetTokens.nextToken();
				if (!(nextBaseToken.equals(nextTargetToken))) {
					while (true) {
						result = result.concat("../");
						if (!baseTokens.hasMoreTokens()) {
							break;
						}
						nextBaseToken = baseTokens.nextToken();
					}
					while (true) {
						result = result.concat(nextTargetToken + "/");
						if (!targetTokens.hasMoreTokens()) {
							break;
						}
						nextTargetToken = targetTokens.nextToken();
					}
					final String temp = target.getFile();
					result = result.concat(temp.substring(temp.lastIndexOf("/") + 1, temp.length()));
					return result;
				}
			}
			while (baseTokens.hasMoreTokens()) {
				result = result.concat("../");
				baseTokens.nextToken();
			}
			while (targetTokens.hasMoreTokens()) {
				nextTargetToken = targetTokens.nextToken();
				result = result.concat(nextTargetToken + "/");
			}
			final String temp = target.getFile();
			result = result.concat(temp.substring(temp.lastIndexOf("/") + 1, temp.length()));
			return result;
		}
		return target.toString();
	}

	/**
	 * This is a correction of a method getFile of a class URL. Namely, on
	 * Windows it returned file paths like /C: etc., which are not valid on
	 * Windows. This correction is heuristic to a great extend. One of the
	 * reasons is that file: something every browser and every system uses
	 * slightly differently.
	 */
	public static String urlGetFile(final URL url) {
		final String osNameStart = System.getProperty("os.name").substring(0, 3);
		if (osNameStart.equals("Win") && url.getProtocol().equals("file")) {
			final String fileName = url.toString().replaceFirst("^file:", "").replace('/', '\\');
			return (fileName.indexOf(':') >= 0) ? fileName.replaceFirst("^\\\\*", "") : fileName;
		}
		else {
			return url.getFile();
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

	public String getRestoreable(final MapModel map) {
		return null;
	}

	public void handleLoadingException(final Exception ex) {
		final String exceptionType = ex.getClass().getName();
		if (exceptionType.equals("freeplane.main.XMLParseException")) {
			final int showDetail = JOptionPane.showConfirmDialog(getController().getViewController().getMapView(),
			    ResourceBundles.getText("map_corrupted"), "Freeplane", JOptionPane.YES_NO_OPTION,
			    JOptionPane.ERROR_MESSAGE);
			if (showDetail == JOptionPane.YES_OPTION) {
				getController().errorMessage(ex);
			}
		}
		else if (exceptionType.equals("java.io.FileNotFoundException")) {
			getController().errorMessage(ex.getMessage());
		}
		else {
			LogTool.severe(ex);
			getController().errorMessage(ex);
		}
	}

	public NodeModel load(final URL url, final MapModel map) {
		NodeModel root = null;
		InputStreamReader urlStreamReader = null;
		try {
			urlStreamReader = new InputStreamReader(url.openStream());
		}
		catch (final AccessControlException ex) {
			getController().errorMessage("Could not open URL " + url.toString() + ". Access Denied.");
			System.err.println(ex);
			return null;
		}
		catch (final Exception ex) {
			getController().errorMessage("Could not open URL " + url.toString() + ".");
			System.err.println(ex);
			return null;
		}
		try {
			root = modeController.getMapController().getMapReader().createNodeTreeFromXml(map, urlStreamReader,
			    Mode.FILE);
			urlStreamReader.close();
			return root;
		}
		catch (final Exception ex) {
			System.err.println(ex);
			return null;
		}
	}

	public void setLastCurrentDir(final File lastCurrentDir) {
		UrlManager.lastCurrentDir = lastCurrentDir;
	}

	public void startup() {
	}
}
