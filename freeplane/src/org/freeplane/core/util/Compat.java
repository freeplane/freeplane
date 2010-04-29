package org.freeplane.core.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.MenuBuilder;

/**
 * Provides methods and constants which are dependend on the underlying java version
 * 
 * @author robert.ladstaetter
 */
public class Compat {
	public static final String JAVA_VERSION = System.getProperty("java.version");
	public static final String VERSION_1_6_0 = "1.6.0";

	private static enum OS {
		MAC, WINDOWS, OTHER
	};

	private static OS os = null;

	public static URL fileToUrl(final File pFile) throws MalformedURLException {
		return pFile.toURL();
	}

	public static boolean isLowerJdk(final String version) {
		return JAVA_VERSION.compareTo(version) < 0;
	}

	public static boolean isMacOsX() {
		Compat.initOS();
		return os.equals(OS.MAC);
	}

	private static void initOS() {
		if (os == null) {
			String osProperty;
			try {
				osProperty = System.getProperty("os.name");
			}
			catch (final SecurityException e) {
				osProperty = "";
			}
			String debugOsName;
			try {
				debugOsName = System.getProperty("freeplane.debug.os.name", "");
			}
			catch (final SecurityException e) {
				debugOsName = "";
			}
			if (osProperty.startsWith("Mac OS") || debugOsName.startsWith("Mac")) {
				os = OS.MAC;
				return;
			}
			if (osProperty.startsWith("Windows") || debugOsName.startsWith("Windows")) {
				os = OS.WINDOWS;
				return;
			}
			os = OS.OTHER;
		}
	}

	public static boolean isWindowsOS() {
		Compat.initOS();
		return os.equals(OS.WINDOWS);
	}

	/**
	 * This is a correction of a method getFile of a class URL. Namely, on
	 * Windows it returned file paths like /C: etc., which are not valid on
	 * Windows. This correction is heuristic to a great extend. One of the
	 * reasons is that file: something every browser and every system uses
	 * slightly differently.
	 */
	private static String urlGetFile(final URL url) {
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

	public static void macAppChanges(final Controller controller) {
		if (!Compat.isMacOsX()) {
			return;
		}
		try {
			final Class<?> macChanges = controller.getClass().getClassLoader().loadClass(
			    "org.freeplane.plugin.macos.MacChanges");
			final Method method = macChanges.getMethod("apply", Controller.class);
			method.invoke(null, controller);
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void macMenuChanges(final Controller controller) {
		if (!Compat.isMacOsX()) {
			return;
		}
		final Set<String> modes = controller.getModes();
		for (final String mode : modes) {
			final MenuBuilder builder = controller.getModeController(mode).getUserInputListenerFactory()
			    .getMenuBuilder();
			final String[] keys = { "/map_popup/toolbars/ToggleMenubarAction", "/menu_bar/file/quit",
			        "/menu_bar/extras/first/options/PropertyAction", "/menu_bar/help/doc/AboutAction" };
			for (final String key : keys) {
				if (builder.contains(key)) {
					builder.removeElement(key);
				}
			}
		}
	}
}
