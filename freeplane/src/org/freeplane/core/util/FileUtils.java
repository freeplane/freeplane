package org.freeplane.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;

public class FileUtils {
	public static void copyFromURL(final URL resource, final File destinationDirectory) {
		final String path = resource.getPath();
		final int index = path.lastIndexOf('/');
		final String fileName = index > -1 ? path.substring(index + 1) : path;
		try {
			final InputStream in = resource.openStream();
			final OutputStream out = new FileOutputStream(new File(destinationDirectory, fileName));
			FileUtils.copyStream(in, out);
		}
		catch (final Exception e) {
			LogUtils.severe("File not found or could not be copied. " + "Was searching for " + path
			        + " and should go to " + destinationDirectory.getAbsolutePath());
		}
	}

	/**
	 */
	public static void copyFromResource(final String prefix, final String fileName, final File destinationDirectory) {
		final String pathToResource = prefix + fileName;
		try {
			final URL resource;
			if (pathToResource.startsWith("file:")) {
				resource = new URL(pathToResource);
			}
			else {
				resource = ResourceController.getResourceController().getResource(pathToResource);
			}
			if (resource == null) {
				LogUtils.severe("Cannot find resource: " + pathToResource);
				return;
			}
			final InputStream in = new BufferedInputStream(resource.openStream());
			final OutputStream out = new FileOutputStream(new File(destinationDirectory, fileName));
			FileUtils.copyStream(in, out);
		}
		catch (final Exception e) {
			LogUtils.severe("File not found or could not be copied. " + "Was searching for " + pathToResource
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
			props.load(FileUtils.class.getResource(classpathRessource).openStream());
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return props;
	}

	public static String slurpFile(final File file) throws IOException {
		FileReader in = null;
		try {
			in = new FileReader(file);
			final StringBuilder builder = new StringBuilder();
			final char[] buf = new char[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				builder.append(buf, 0, len);
			}
			final String result = builder.toString();
			return result;
		}
		finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public static String slurpFile(final String fileName) throws IOException {
		return FileUtils.slurpFile(new File(fileName));
	}

	public static Charset defaultCharset() {
		try {
			final String defaultCharsetName = ResourceController.getResourceController().getProperty("default_charset");
			if (defaultCharsetName.equals("JVMdefault")) {
				return Charset.defaultCharset();
			}
			return Charset.forName(defaultCharsetName);
		}
		catch (final Exception e) {
			return Charset.defaultCharset();
		}
	}

	/**
	 * Returns the lowercase of the extension of a file.
	 */
	public static String getExtension(final File f) {
		return FileUtils.getExtension(f.toString());
	}

	/**
	 * Returns the lowercase of the extension of a file.
	 */
	public static String getExtension(final String s) {
		if (s == null) {
			return null;
		}
		final int i = s.lastIndexOf('.');
		return (i > 0 && i < s.length() - 1) ? s.substring(i + 1).toLowerCase().trim() : "";
	}

	public static boolean isAbsolutePath(final String path) {
		final String osNameStart = System.getProperty("os.name").substring(0, 3);
		final String fileSeparator = System.getProperty("file.separator");
		if (osNameStart.equals("Win")) {
			return ((path.length() > 1) && path.substring(1, 2).equals(":")) || path.startsWith(fileSeparator);
		}
		else if (osNameStart.equals("Mac")) {
			return path.startsWith(fileSeparator);
		}
		else {
			return path.startsWith(fileSeparator);
		}
	}

	/**
	 * In case of trouble, the method returns null.
	 *
	 * @param pInputFile
	 *            the file to read.
	 * @return the complete content of the file. or null if an exception has
	 *         occured.
	 */
	public static String readFile(final File pInputFile) {
		final StringBuilder lines = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(pInputFile));
			final String endLine = System.getProperty("line.separator");
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				lines.append(line).append(endLine);
			}
			bufferedReader.close();
		}
		catch (final Exception e) {
			LogUtils.severe(e);
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				}
				catch (final Exception ex) {
					LogUtils.severe(ex);
				}
			}
			return null;
		}
		return lines.toString();
	}

	public static String removeExtension(final String s) {
		final int i = s.lastIndexOf('.');
		return (i > 0 && i < s.length() - 1) ? s.substring(0, i) : s;
	}

	public static void setHidden(final File file, final boolean hidden, final boolean synchronously) {
		final String osNameStart = System.getProperty("os.name").substring(0, 3);
		if (osNameStart.equals("Win")) {
			try {
				Controller.exec("attrib " + (hidden ? "+" : "-") + "H \"" + file.getAbsolutePath() + "\"");
				if (!synchronously) {
					return;
				}
				int timeOut = 10;
				while (file.isHidden() != hidden && timeOut > 0) {
					Thread.sleep(10/* miliseconds */);
					timeOut--;
				}
			}
			catch (final Exception e) {
				LogUtils.severe(e);
			}
		}
	}
}
