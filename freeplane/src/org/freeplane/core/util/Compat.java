package org.freeplane.core.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.mode.Controller;

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
	public static final Set<String> executableExtensions = new HashSet<String>(Arrays.asList(new String[] { "exe",
	        "com", "vbs", "bat", "lnk" }));

	public static boolean isExecutable(final String linkText) {
		if (linkText == null) {
			return false;
		}
		final String osNameStart = System.getProperty("os.name").substring(0, 3);
		return osNameStart.equals("Win")
		        && executableExtensions.contains(FileUtils.getExtension(linkText.toLowerCase()));
	}

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

	public static void macAppChanges() {
		if (!Compat.isMacOsX()) {
			return;
		}
		try {
			final Class<?> macChanges = Controller.class.getClassLoader().loadClass(
			    "org.freeplane.plugin.macos.MacChanges");
			final Method method = macChanges.getMethod("apply", Controller.class);
			method.invoke(null, Controller.getCurrentController());
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void macMenuChanges() {
		if (!Compat.isMacOsX()) {
			return;
		}
		final Controller controller = Controller.getCurrentController();
		final Set<String> modes = controller .getModes();
		for (final String mode : modes) {
			final MenuBuilder builder = controller.getModeController(mode).getUserInputListenerFactory()
			    .getMenuBuilder();
			final String[] keys = { 
					"MB_ToggleMenubarAction", 
					"MP_ToggleMenubarAction", 
					"MB_QuitAction",
			        "MB_PropertyAction", 
			        "MB_AboutAction" 
			};
			for (final String key : keys) {
				if (builder.contains(key)) {
					builder.removeElement(key);
				}
			}
		}
	}
	final private static String PREVIEW_DIR=File.separatorChar + "1.2.x";
	
	public static String getFreeplaneUserDirectory() {
		Properties freeplaneProperties = new Properties();
		try {
			freeplaneProperties.load(Compat.class.getClassLoader().getResourceAsStream(ResourceController.FREEPLANE_PROPERTIES));
		} catch (IOException e) {
			LogUtils.warn(e);
		}
		String applicationName = freeplaneProperties.getProperty("ApplicationName", "freeplane").toLowerCase(Locale.ENGLISH);
		String userFpDir = null;
		if(applicationName.equalsIgnoreCase("freeplane")){
			userFpDir = System.getProperty("org.freeplane.userfpdir");
		}
		else if(System.getProperty("org.freeplane.userfpdir") != null && System.getProperty("org.freeplane.userfpdir").endsWith("Data")){
			userFpDir = System.getProperty("org.freeplane.userfpdir") + File.separator + freeplaneProperties.getProperty("ApplicationName", "Freeplane");
		}
		else if(System.getenv("APPDATA") != null && System.getenv("APPDATA").length() > 0){			
			userFpDir = System.getenv("APPDATA") + File.separator + freeplaneProperties.getProperty("ApplicationName", "Freeplane");
		}
		if(userFpDir == null){						
			userFpDir = System.getProperty("user.home")+ File.separator + "." + applicationName;			
		}
		if(PREVIEW_DIR != null)
			return userFpDir + PREVIEW_DIR;
		return userFpDir;
	}

	static public String smbUri2unc(final URI uri) {
		String uriString;
 		uriString = ("//" + uri.getHost() + uri.getPath()) .replace('/', '\\');
		final String fragment = uri.getFragment();
		if(fragment != null)
			uriString = uriString + '#' + fragment;
		return uriString;
	}

}
