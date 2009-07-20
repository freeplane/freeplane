package org.freeplane.core.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.freeplane.core.ui.components.UITools;

/**
 * Provides methods and constants which are dependend on the underlying java version
 * 
 * @author robert.ladstaetter
 */
public class Compat {
	public static final String JAVA_VERSION = System.getProperty("java.version");
	public static final String VERSION_1_5_0 = "1.5.0";
	public static final String VERSION_1_6_0 = "1.6.0";

	public static void checkJavaVersion() {
		if (Compat.isLowerJdk(VERSION_1_5_0)) {
			final String message = "Warning: Freeplane requires version Java 1.5.0 or higher (your version: "
			        + JAVA_VERSION + ", installed in " + System.getProperty("java.home") + ").";
			LogTool.severe(message);
			UITools.errorMessage(message);
			System.exit(1);
		}
	}

	public static URI cleanURI(final URI hyperlink) {
		final String rawPath = hyperlink.getRawPath();
		if (rawPath == null) {
			return hyperlink;
		}
		final int indexOfColon = rawPath.indexOf(':', 3);
		if (indexOfColon == -1) {
			return hyperlink;
		}
		final String scheme = hyperlink.getScheme();
		if (scheme != null && !scheme.equals("file")) {
			return hyperlink;
		}
		final String osNameStart = System.getProperty("os.name").substring(0, 3);
		if (!osNameStart.equals("Win")) {
			return hyperlink;
		}
		URI uri;
		try {
			uri = new URI("file:/" + rawPath.substring(indexOfColon - 1));
			return uri;
		}
		catch (final URISyntaxException e) {
			LogTool.severe(e);
		}
		return hyperlink;
	}

	public static URL fileToUrl(final File pFile) throws MalformedURLException {
		return pFile.toURL();
	}

	public static boolean isLowerJdk(final String version) {
		return JAVA_VERSION.compareTo(version) < 0;
	}

	public static boolean isMacOsX() {
		boolean underMac = false;
		final String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) {
			underMac = true;
		}
		return underMac;
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
		return new File(Compat.urlGetFile(pUrl));
	}

	public static void useScreenMenuBar() {
		/* This is only for apple but does not harm for the others. */
		//		if (isMacOsX()) 
		System.setProperty("apple.laf.useScreenMenuBar", "true");
	}
}
