package org.freeplane.core.util;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.freeplane.features.mode.Controller;

/**
 * Provides methods and constants which are dependent on the underlying java version
 *
 * @author robert.ladstaetter
 */
public class Compat {
	public static final String FREEPLANE_USERDIR_PROPERTY = "org.freeplane.userfpdir";
    public static final String JAVA_VERSION = System.getProperty("java.version");
	public static final String VERSION_1_6_0 = "1.6.0";

	private static enum OS {
		MAC, WINDOWS, OTHER
	};

	private static OS os = null;
	public static final Set<String> executableExtensions = new HashSet<String>(Arrays.asList(new String[] { "exe",
	        "com", "vbs", "bat", "lnk", "cmd" }));

	public static boolean isWindowsExecutable(final URI link) {
		if (link == null
				|| !"file".equalsIgnoreCase(link.getScheme())) {
			return false;
		}
		return isWindowsOS() && executableExtensions.contains(FileUtils.getExtension(link.toString()));
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
		if( !url.getProtocol().equals("file"))
			return null;
		String fileName = url.toString().replaceFirst("^file:", "");
		final String osNameStart = System.getProperty("os.name").substring(0, 3);
		if (osNameStart.equals("Win") && url.getProtocol().equals("file")) {
			fileName = fileName.replace('/', File.separatorChar);
			return (fileName.indexOf(':') >= 0) ? fileName.replaceFirst("^\\\\*", "") : fileName;
		}
		else {
			return fileName;
		}
	}

	public static File urlToFile(final URL pUrl) {
		final String path = Compat.urlGetFile(pUrl);
		if(path != null)
			return new File(path);
		else
			return null;
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

	final public static String CURRENT_VERSION_DIR= File.separatorChar + "1.8.x";
	final public static String PREVIOUS_VERSION_DIR_NAME = "1.7.x";

	private static String userFpDir = null;


	/** the directory *including* the version directory. */
	public static String getApplicationUserDirectory() {
		return getApplicationUserDirectoryExcludingVersion() + CURRENT_VERSION_DIR;
	}

	public static String getApplicationUserDirectoryExcludingVersion() {
		if(userFpDir == null)
			findApplicationUserDirectory();
		return userFpDir;
	}

	protected static void findApplicationUserDirectory() {
		final String userFpDirByProperty = System.getProperty(FREEPLANE_USERDIR_PROPERTY);
		final String userFpDirPath = userFpDirByProperty != null ? userFpDirByProperty : getDefaultFreeplaneUserDirectory();
		try {
			userFpDir = new File(userFpDirPath).getCanonicalPath();
		} catch (IOException e) {
			try {
				userFpDir = new File(getDefaultFreeplaneUserDirectory()).getCanonicalPath();
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	public static String getDefaultFreeplaneUserDirectory() {
        return System.getProperty("user.home")+ File.separator + ".freeplane";
    }

	static public String smbUri2unc(final URI uri) {
		String uriString;
 		uriString = ("//" + uri.getHost() + uri.getPath()) .replace('/', '\\');
		final String fragment = uri.getFragment();
		if(fragment != null)
			uriString = uriString + '#' + fragment;
		return uriString;
	}

	static public boolean isPlainEvent(final MouseEvent e) {
        final int modifiers = getModifiers(e);
        return modifiers == 0;
    }

	private static int getModifiers(final MouseEvent e) {
	    return e.getModifiersEx() &
        		(InputEvent.CTRL_DOWN_MASK
        				| InputEvent.META_DOWN_MASK
        				| InputEvent.SHIFT_DOWN_MASK
        				| InputEvent.ALT_DOWN_MASK
        				);
    }

	static public boolean isCtrlEvent(final MouseEvent e) {
         return isExtendedCtrlEvent(e, 0);
    }

	public static boolean isCtrlShiftEvent(MouseEvent e) {
		return isExtendedCtrlEvent(e, InputEvent.SHIFT_DOWN_MASK);
    }

	public static boolean isCtrlAltEvent(MouseEvent e) {
		return isExtendedCtrlEvent(e, InputEvent.ALT_DOWN_MASK);
    }

	static private boolean isExtendedCtrlEvent(final MouseEvent e, int otherModifiers) {
        final int modifiers = getModifiers(e);
		if (isMacOsX())
        	return modifiers == (InputEvent.META_DOWN_MASK | otherModifiers);
        return modifiers == (InputEvent.CTRL_DOWN_MASK|otherModifiers);
    }

	public static boolean isShiftEvent(MouseEvent e) {
        final int modifiers = getModifiers(e);
        return modifiers == InputEvent.SHIFT_DOWN_MASK;
    }
}
