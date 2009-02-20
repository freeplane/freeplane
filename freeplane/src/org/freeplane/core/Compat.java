package org.freeplane.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.FreeplaneVersion;
import org.freeplane.core.enums.ResourceControllerProperties;
import org.freeplane.core.url.UrlManager;

/**
 * Provides methods and constants which are dependend on the underlying java version
 * 
 * @author robert.ladstaetter
 */
public class Compat {

	private static final String VERSION_1_5_0 = "1.5.0";

	private static final String VERSION_1_6_0 = "1.6.0";

	public static final String JAVA_VERSION = System.getProperty("java.version");

	public static final boolean NEED_PREF_SIZE_BUG_FIX = JAVA_VERSION.compareTo(VERSION_1_5_0) < 0;

	public static File urlToFile(final URL pUrl) throws URISyntaxException {
		if (isLowerJdk(VERSION_1_6_0)) {
			return new File(UrlManager.urlGetFile(pUrl));
		}
		return new File(new URI(pUrl.toString()));
	}

	public static URL fileToUrl(final File pFile) throws MalformedURLException {
		if (isLowerJdk(VERSION_1_6_0)) {
			return pFile.toURL();
		}
		return pFile.toURI().toURL();
	}

	public static boolean isLowerJdk(String version) {
		return JAVA_VERSION.compareTo(version) < 0;
	}

	public static void checkJavaVersion() {
		System.out.println("Checking Java Version...");
		if (isLowerJdk(VERSION_1_5_0)) {
			final String message = "Warning: Freeplane requires version Java 1.5.0 or higher (your version: "
			        + JAVA_VERSION + ", installed in " + System.getProperty("java.home") + ").";
			System.err.println(message);
			JOptionPane.showMessageDialog(null, message, "Freeplane", JOptionPane.WARNING_MESSAGE);
			System.exit(1);
		}
	}

	public static void showSysInfo() {
		final StringBuffer info = new StringBuffer();
		info.append("freeplane_version = ");
		info.append(FreeplaneVersion.getVersion());
		info.append("; freeplane_xml_version = ");
		info.append(ResourceControllerProperties.XML_VERSION);
		info.append("\njava_version = ");
		info.append(System.getProperty("java.version"));
		info.append("; os_name = ");
		info.append(System.getProperty("os.name"));
		info.append("; os_version = ");
		info.append(System.getProperty("os.version"));
	}

	public static boolean isMacOsX() {
		boolean underMac = false;
		final String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) {
			underMac = true;
		}
		return underMac;
	}

	public static void useScreenMenuBar() {
		/* This is only for apple but does not harm for the others. */
		//		if (isMacOsX()) 
		System.setProperty("apple.laf.useScreenMenuBar", "true");
	}
}
