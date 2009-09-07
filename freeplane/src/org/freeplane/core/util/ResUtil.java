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
	
	/**
	 */
	public static void copyFromFile(final String dir, final String fileName, final String destinationDirectory) {
		try {
			final File resource = new File(dir, fileName);
			if (resource == null) {
				LogTool.severe("Cannot find resource: " + dir + fileName);
				return;
			}
			copyFromFile(resource, destinationDirectory);
		}
		catch (final Exception e) {
			LogTool.severe("File not found or could not be copied. " + "Was searching for " + dir + fileName
			        + " and should go to " + destinationDirectory);
		}
	}
	
	public static void copyFromFile(final File resource, final String destinationDirectory) {
		try {
			final InputStream in = new FileInputStream(resource);
			final OutputStream out = new FileOutputStream(destinationDirectory + "/" + resource.getName());
			ResUtil.copyStream(in, out);
		}
		catch (final Exception e) {
			LogTool.severe("File not found or could not be copied. " + "Was searching for " + resource.getPath()
			        + " and should go to " + destinationDirectory);
		}
	}
	
	public static void copyFromURL(final URL resource, final String destinationDirectory) {
		String path = resource.getPath();
		int index   = path.lastIndexOf('/');
		String fileName = index > -1 ? path.substring(index + 1) : path;
		
		try {
			final InputStream in = resource.openStream();
			final OutputStream out = new FileOutputStream(destinationDirectory + "/" + fileName);
			ResUtil.copyStream(in, out);
		}
		catch (final Exception e) {
			LogTool.severe("File not found or could not be copied. " + "Was searching for " + path
			        + " and should go to " + destinationDirectory);
		}
	}

	/**
	 */
	public static void copyFromResource(final String prefix, final String fileName, final String destinationDirectory) {
		try {
			final URL resource = ResourceController.getResourceController().getResource(prefix + fileName);
			if (resource == null) {
				LogTool.severe("Cannot find resource: " + prefix + fileName);
				return;
			}
			final InputStream in = new BufferedInputStream(resource.openStream());
			final OutputStream out = new FileOutputStream(destinationDirectory + "/" + fileName);
			ResUtil.copyStream(in, out);
		}
		catch (final Exception e) {
			LogTool.severe("File not found or could not be copied. " + "Was searching for " + prefix + fileName
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
