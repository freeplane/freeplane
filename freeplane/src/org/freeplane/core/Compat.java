package org.freeplane.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JOptionPane;

import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.LogTool;

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
			JOptionPane.showMessageDialog(null, message, "Freeplane", JOptionPane.WARNING_MESSAGE);
			System.exit(1);
		}
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

	public static File urlToFile(final URL pUrl) throws URISyntaxException {
		if (Compat.isLowerJdk(VERSION_1_6_0)) {
			return new File(UrlManager.urlGetFile(pUrl));
		}
		return new File(new URI(pUrl.toString()));
	}

	public static void useScreenMenuBar() {
		/* This is only for apple but does not harm for the others. */
		//		if (isMacOsX()) 
		System.setProperty("apple.laf.useScreenMenuBar", "true");
	}
}
