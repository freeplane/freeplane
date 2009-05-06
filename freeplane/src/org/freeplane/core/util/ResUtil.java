package org.freeplane.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import org.freeplane.core.resources.ResourceController;

public class ResUtil {
	/**
	 */
	public static void copyFromFile(final String dir, final String fileName, final String destinationDirectory) {
		try {
			final File resource = new File(dir, fileName);
			if (resource == null) {
				Logger.global.severe("Cannot find resource: " + dir + fileName);
				return;
			}
			final InputStream in = new FileInputStream(resource);
			final OutputStream out = new FileOutputStream(destinationDirectory + "/" + fileName);
			ResUtil.copyStream(in, out);
		}
		catch (final Exception e) {
			Logger.global.severe("File not found or could not be copied. " + "Was earching for " + dir + fileName
			        + " and should go to " + destinationDirectory);
		}
	}

	/**
	 */
	public static void copyFromResource(final String prefix, final String fileName, final String destinationDirectory) {
		try {
			final URL resource = ResourceController.getResourceController().getResource(prefix + fileName);
			if (resource == null) {
				Logger.global.severe("Cannot find resource: " + prefix + fileName);
				return;
			}
			final InputStream in = resource.openStream();
			final OutputStream out = new FileOutputStream(destinationDirectory + "/" + fileName);
			ResUtil.copyStream(in, out);
		}
		catch (final Exception e) {
			Logger.global.severe("File not found or could not be copied. " + "Was earching for " + prefix + fileName
			        + " and should go to " + destinationDirectory);
		}
	}

	public static void copyStream(final InputStream in, final OutputStream out) throws IOException {
		final byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 */
	public static boolean createDirectory(final String directoryName) {
		final File dir = new File(directoryName);
		if (!dir.exists()) {
			return dir.mkdir();
		}
		return true;
	}

	public static Properties loadProperties(final String classpathRessource) {
		final Properties versionProperties = new Properties();
		try {
			versionProperties.load(ResUtil.class.getResource(classpathRessource).openStream());
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return versionProperties;
	}
}
