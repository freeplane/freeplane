package org.freeplane.core.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import org.freeplane.core.resources.ResourceController;

public class ResUtil {
	
	public static void copyFromURL(final URL resource, final File destinationDirectory) {
		String path = resource.getPath();
		int index   = path.lastIndexOf('/');
		String fileName = index > -1 ? path.substring(index + 1) : path;
		
		try {
			final InputStream in = resource.openStream();
			final OutputStream out = new FileOutputStream(new File(destinationDirectory, fileName));
			ResUtil.copyStream(in, out);
		}
		catch (final Exception e) {
			LogTool.severe("File not found or could not be copied. " + "Was searching for " + path
			        + " and should go to " + destinationDirectory.getAbsolutePath());
		}
	}

	/**
	 */
	public static void copyFromResource(final String prefix, final String fileName, final File destinationDirectory) {
		try {
			final URL resource = ResourceController.getResourceController().getResource(prefix + fileName);
			if (resource == null) {
				LogTool.severe("Cannot find resource: " + prefix + fileName);
				return;
			}
			final InputStream in = new BufferedInputStream(resource.openStream());
			final OutputStream out = new FileOutputStream(new File(destinationDirectory, fileName));
			ResUtil.copyStream(in, out);
		}
		catch (final Exception e) {
			LogTool.severe("File not found or could not be copied. " + "Was searching for " + prefix + fileName
			        + " and should go to " + destinationDirectory.getAbsolutePath());
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
			return dir.mkdirs();
		}
		return true;
	}

	public static Properties loadProperties(final String classpathRessource) {
		final Properties props = new Properties();
		try {
			props.load(ResUtil.class.getResource(classpathRessource).openStream());
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return props;
	}
}
