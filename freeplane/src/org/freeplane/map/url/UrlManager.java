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
package org.freeplane.map.url;

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
import java.net.URI;
import java.net.URISyntaxException;
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
import org.freeplane.core.map.MapModel;
import org.freeplane.core.map.ModeController;
import org.freeplane.core.map.NodeModel;

/**
 * @author Dimitry Polivaev
 */
public class UrlManager implements IExtension {
	private static File lastCurrentDir = null;

	/**
	 * Example: expandPlaceholders("Hello $1.","Dolly"); => "Hello Dolly."
	 */
	public static String expandPlaceholders(final String message, String s1) {
		String result = message;
		if (s1 != null) {
			s1 = s1.replaceAll("\\\\", "\\\\\\\\");
			result = result.replaceAll("\\$1", s1);
		}
		return result;
	}

	public static String expandPlaceholders(final String message, final String s1, final String s2) {
		String result = message;
		if (s1 != null) {
			result = result.replaceAll("\\$1", s1);
		}
		if (s2 != null) {
			result = result.replaceAll("\\$2", s2);
		}
		return result;
	}

	public static URL fileToUrl(final File pFile) throws MalformedURLException {
		if (Controller.JAVA_VERSION.compareTo("1.6.0") < 0) {
			return pFile.toURL();
		}
		return pFile.toURI().toURL();
	}

	/**
	 * Creates a default reader that just reads the given file.
	 *
	 * @throws FileNotFoundException
	 */
	public static Reader getActualReader(final File file) throws FileNotFoundException {
		return new BufferedReader(new FileReader(file));
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
			org.freeplane.core.util.Tools.logException(e);
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				}
				catch (final Exception ex) {
					org.freeplane.core.util.Tools.logException(ex);
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
	public static Reader getUpdateReader(final File file, final String xsltScript)
	        throws IOException {
		StringWriter writer = null;
		InputStream inputStream = null;
		boolean successful = false;
		try {
			URL updaterUrl = null;
			updaterUrl = Controller.getResourceController().getResource(xsltScript);
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
						org.freeplane.core.util.Tools.logException(ex);
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

	public static boolean isAbsolutePath(final String path) {
		final String osNameStart = System.getProperty("os.name").substring(0, 3);
		final String fileSeparator = System.getProperty("file.separator");
		if (osNameStart.equals("Win")) {
			return ((path.length() > 1) && path.substring(1, 2).equals(":"))
			        || path.startsWith(fileSeparator);
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
				Runtime.getRuntime().exec(
				    "attrib " + (hidden ? "+" : "-") + "H \"" + file.getAbsolutePath() + "\"");
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
				org.freeplane.core.util.Tools.logException(e);
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
		if ((base.getProtocol().equals(target.getProtocol()))
		        && (base.getHost().equals(target.getHost()))) {
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
					result = result
					    .concat(temp.substring(temp.lastIndexOf("/") + 1, temp.length()));
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

	public static File urlToFile(final URL pUrl) throws URISyntaxException {
		if (Controller.JAVA_VERSION.compareTo("1.6.0") < 0) {
			return new File(UrlManager.urlGetFile(pUrl));
		}
		return new File(new URI(pUrl.toString()));
	}

	final private ModeController modeController;

	public UrlManager(final ModeController modeController) {
		super();
		this.modeController = modeController;
		createActions();
	}

	/**
	 *
	 */
	private void createActions() {
		modeController.addAction("open", new OpenAction());
		modeController.addAction("save", new SaveAction());
		modeController.addAction("saveAs", new SaveAsAction());
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
		final MapModel map = Controller.getController().getMap();
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
		if (exceptionType.equals("freemind.main.XMLParseException")) {
			final int showDetail = JOptionPane.showConfirmDialog(modeController.getMapView().getComponent(),
			    modeController.getText("map_corrupted"), "FreeMind", JOptionPane.YES_NO_OPTION,
			    JOptionPane.ERROR_MESSAGE);
			if (showDetail == JOptionPane.YES_OPTION) {
				Controller.getController().errorMessage(ex);
			}
		}
		else if (exceptionType.equals("java.io.FileNotFoundException")) {
			Controller.getController().errorMessage(ex.getMessage());
		}
		else {
			org.freeplane.core.util.Tools.logException(ex);
			Controller.getController().errorMessage(ex);
		}
	}

	public NodeModel load(final URL url, final MapModel map) {
		NodeModel root = null;
		InputStreamReader urlStreamReader = null;
		try {
			urlStreamReader = new InputStreamReader(url.openStream());
		}
		catch (final AccessControlException ex) {
			Controller.getController().errorMessage(
			    "Could not open URL " + url.toString() + ". Access Denied.");
			System.err.println(ex);
			return null;
		}
		catch (final Exception ex) {
			Controller.getController().errorMessage("Could not open URL " + url.toString() + ".");
			System.err.println(ex);
			return null;
		}
		try {
			root = modeController.getMapController().createNodeTreeFromXml((map), urlStreamReader);
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

	public static void install(ModeController modeController, UrlManager urlManager) {
		modeController.addExtension(UrlManager.class, urlManager);
    }

	public static UrlManager getController(ModeController modeController) {
		return (UrlManager)modeController.getExtension(UrlManager.class);
    }
}
