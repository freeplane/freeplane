package org.freeplane.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mode.Controller;

public class FileUtils {
	public static void copyFromURL(final URL resource, final File destinationDirectory) {
		final String path = resource.getPath();
		final int index = path.lastIndexOf('/');
		final String fileName = URLDecoder.decode(index > -1 ? path.substring(index + 1) : path);
		try (InputStream in = resource.openStream();
		    OutputStream out = new FileOutputStream(new File(destinationDirectory, fileName))) {
		        FileUtils.copyStream(in, out);
		}
		catch (final Exception e) {
			LogUtils.severe("File not found or could not be copied. " + "Was searching for " + path
			        + " and should go to " + destinationDirectory.getAbsolutePath());
		}
	}

	public static void copyFromResource(final String path, final String fileName, final File destinationDirectory) {
		final String pathToResource = path + fileName;
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
            try (InputStream in = new BufferedInputStream(resource.openStream());
                    OutputStream out = new FileOutputStream(new File(destinationDirectory, fileName))) {
                    FileUtils.copyStream(in, out);
            }
		}
		catch (final Exception e) {
			LogUtils.severe("File not found or could not be copied. " + "Was searching for " + pathToResource
			        + " and should go to " + destinationDirectory.getAbsolutePath());
		}
	}

	/** the caller has to close the streams. */
	public static void copyStream(final InputStream in, final OutputStream out) throws IOException {
		final byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
	}

	public static void dumpStringToFile(final String string, final File outFile, String encoding) throws IOException {
        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outFile), encoding)){
			out.write(string);
		}
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
		try (InputStream in = FileUtils.class.getResource(classpathRessource).openStream()){
			props.load(in);
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return props;
	}

	private static String slurp(final Reader reader) throws IOException {
		/* read data into a string */
		final StringBuilder builder = new StringBuilder();
		final char[] buf = new char[1024];
		int len;
		while ((len = reader.read(buf)) > 0) {
			builder.append(buf, 0, len);
		}
		final String result = builder.toString();
		return result;
	}

	public static String slurpResource(final URL resource) throws IOException {
		/* read the `resource` into s atring */
		try (InputStream instream = resource.openStream()){
			final BufferedReader input = new BufferedReader(new InputStreamReader(
			            instream, StandardCharsets.UTF_8));
			return slurp(input);
		}
	}

	final private static Map<String, String> cachedResources = new HashMap<>();

	public static String slurpResource(final String fileName) throws IOException {
		if (cachedResources.containsKey(fileName))
			return cachedResources.get(fileName);
		/* read the resource `fileName` into s atring */
		final URL resource = ResourceController.getResourceController().getResource(fileName);
		if (resource == null) {
			LogUtils.severe("Cannot find resource: " + fileName);
			return "";
		}
		final String slurpedResource = FileUtils.slurpResource(resource);
		cachedResources.put(fileName, slurpedResource);
		return slurpedResource;
	}

	public static String slurpFile(final File file) throws IOException {
		try (FileReader in = new FileReader(file)){
			return slurp(in);
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
		for (int i = s.length() - 1; i >= 0; i--) {
			final char c = s.charAt(i);
			if (c == File.separatorChar || c == '/')
				return "";
			if (c == '.') {
				return s.substring(i + 1).trim().toLowerCase();
			}
		}
		return "";
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
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(pInputFile))){
			final String endLine = System.getProperty("line.separator");
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				lines.append(line).append(endLine);
			}
		}
		catch (final Exception e) {
			LogUtils.severe(e);
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

	public static void copyFile(File in, File out) throws IOException {
        try (   FileInputStream fileInputStream = new FileInputStream(in);
                FileOutputStream fileOutputStream = new FileOutputStream(out);
                FileChannel inChannel = fileInputStream.getChannel();
		        FileChannel outChannel = fileOutputStream.getChannel()){
			// inChannel.transferTo(0, inChannel.size(), outChannel);
			// original -- apparently has trouble copying large files on Windows
			// magic number for Windows, (64Mb - 32Kb)
			int maxCount = (64 * 1024 * 1024) - (32 * 1024);
			long size = inChannel.size();
			long position = 0;
			while (position < size) {
				position += inChannel.transferTo(position, maxCount, outChannel);
			}
		}
	}

	public static String validFileNameOf(String proposal) {
		return proposal.replaceAll("[&:/\\\\\0%$#~\\?\\*]+", "");
	}

	public static File getAbsoluteFile(String baseDir, String file) {
    	if(isAbsolutePath(file))
    		return new File(file);
    	else
			return new File(baseDir, file);
	}
}
