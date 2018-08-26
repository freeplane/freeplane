package org.freeplane.launcher;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

class Utils {

	static File getPathToJar(Class<?> clazz) {
		URL frameworkUrl = clazz.getProtectionDomain().getCodeSource().getLocation();
		try {
			return new File(frameworkUrl.toURI()).getCanonicalFile().getParentFile();
		}
		catch (URISyntaxException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	static void setDefineIfNeeded(String name, String value) {
		if (Utils.isDefineNotSet(name)) {
			Utils.setDefine(name, value);
		}
                else {
                    System.out.println(name + "=" + System.getProperty(name));
                }
	}

	static boolean isDefineNotSet(String name) {
		return System.getProperty(name, null) == null;
	}

	static String setDefine(String name, String value) {
		System.out.println("*" + name + "=" + value);
		return System.setProperty(name, value);
	}
}
