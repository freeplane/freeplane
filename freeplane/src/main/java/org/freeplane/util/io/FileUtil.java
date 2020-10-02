package org.freeplane.util.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Provides utility methods to make working with file contents easy.
 */
public class FileUtil
{
	/**
	 * Returns a {@link List} of String instances representing the contents of the file loaded from the given
	 * <code>inputStream</code>, broken into "lines" at each occurrerence of <code>delimiter</code>, discarding any
	 * blank lines.
	 */
	public static List<String> getTokenizedFileContents(InputStream inputStream, String delimiter) throws IOException
	{
		return getTokenizedFileContents(inputStream, delimiter, false);
	}

	/**
	 * Returns a {@link List} of String instances representing the contents of the file loaded from the given
	 * <code>inputStream</code>, broken into "lines" at each occurrerence of <code>delimiter</code>.
	 * 
	 * @param keepBlankLines If true, then blank lines are preserved, otherwise they are discarded.
	 */
	public static List<String> getTokenizedFileContents(InputStream inputStream, String delimiter, boolean keepBlankLines) throws IOException
	{
		String string = FileUtil.getFileContents(inputStream);
		StringTokenizer tokenizer = new StringTokenizer(string, delimiter);
		List<String> tokens = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			String nextToken = tokenizer.nextToken();
			if (keepBlankLines) {
				tokens.add(nextToken);
			}
			else if (!nextToken.trim().equals("")) {
				tokens.add(nextToken);
			}
		}
		return tokens;
	}

	/**
	 * Returns a String representing the contents of the file loaded from the given <code>file</code>.
	 */
	public static String getFileContents(File file) throws FileNotFoundException, IOException
	{
		return getFileContents(file.getAbsolutePath());
	}

	/**
	 * Loads the file named <code>filename</code> and returns its contents as a single String.
	 */
	public static String getFileContents(String filename) throws FileNotFoundException, IOException
	{
		LineNumberReader reader = new LineNumberReader(new FileReader(filename));
		String line = null;
		StringBuffer buffer = new StringBuffer();
		while ((line = reader.readLine()) != null) {
			buffer.append(line).append("\n");
		}
		String fileContents = buffer.toString();
		return fileContents;
	}

	/**
	 * Returns a String representing the contents of the file loaded from the given <code>inputStream</code>.
	 */
	public static String getFileContents(InputStream inputStream) throws IOException
	{
		return new String(getFileBytes(inputStream));
	}

	/**
	 * Loads the file named <code>filename</code> and returns its contents as a byte array.
	 */
	public static byte[] getFileBytes(String filename) throws FileNotFoundException, IOException
	{
		return getFileBytes(new FileInputStream(filename));
	}

	/**
	 * Returns a byte-array representing the contents of the file loaded from the given <code>inputStream</code>.
	 */
	public static byte[] getFileBytes(InputStream inputStream) throws IOException
	{
		BufferedInputStream input = new BufferedInputStream(inputStream);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int b = -1;
		while ((b = input.read()) != -1) {
			output.write(b);
		}
		return output.toByteArray();
	}
}
