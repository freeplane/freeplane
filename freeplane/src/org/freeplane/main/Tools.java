/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.freeplane.controller.Controller;
import org.freeplane.map.tree.view.NodeView;

/**
 * @author foltin
 */
public class Tools {
	public static class BooleanHolder {
		private boolean value;

		public BooleanHolder() {
		}

		public BooleanHolder(final boolean initialValue) {
			value = initialValue;
		}

		public boolean getValue() {
			return value;
		}

		public void setValue(final boolean value) {
			this.value = value;
		}
	}

	// from: http://javaalmanac.com/egs/javax.crypto/PassKey.html
	public static class DesEncrypter {
		private static final int SALT_LENGTH = 8;
		private static final String SALT_PRESENT_INDICATOR = " ";
		Cipher dcipher;
		Cipher ecipher;
		int iterationCount = 19;
		final private String mAlgorithm;
		final private char[] passPhrase;
		byte[] salt = { (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32, (byte) 0x56,
		        (byte) 0x35, (byte) 0xE3, (byte) 0x03 };

		public DesEncrypter(final StringBuffer pPassPhrase, final String pAlgorithm) {
			passPhrase = new char[pPassPhrase.length()];
			pPassPhrase.getChars(0, passPhrase.length, passPhrase, 0);
			mAlgorithm = pAlgorithm;
		}

		public String decrypt(String str) {
			if (str == null) {
				return null;
			}
			try {
				byte[] salt = null;
				final int indexOfSaltIndicator = str.indexOf(DesEncrypter.SALT_PRESENT_INDICATOR);
				if (indexOfSaltIndicator >= 0) {
					final String saltString = str.substring(0, indexOfSaltIndicator);
					str = str.substring(indexOfSaltIndicator + 1);
					salt = Tools.fromBase64(saltString);
				}
				final byte[] dec = Tools.fromBase64(str);
				init(salt);
				final byte[] utf8 = dcipher.doFinal(dec);
				return new String(utf8, "UTF8");
			}
			catch (final javax.crypto.BadPaddingException e) {
			}
			catch (final IllegalBlockSizeException e) {
			}
			catch (final UnsupportedEncodingException e) {
			}
			return null;
		}

		public String encrypt(final String str) {
			try {
				final byte[] utf8 = str.getBytes("UTF8");
				final byte[] newSalt = new byte[DesEncrypter.SALT_LENGTH];
				for (int i = 0; i < newSalt.length; i++) {
					newSalt[i] = (byte) (Math.random() * 256l - 128l);
				}
				init(newSalt);
				final byte[] enc = ecipher.doFinal(utf8);
				return Tools.toBase64(newSalt) + DesEncrypter.SALT_PRESENT_INDICATOR
				        + Tools.toBase64(enc);
			}
			catch (final javax.crypto.BadPaddingException e) {
			}
			catch (final IllegalBlockSizeException e) {
			}
			catch (final UnsupportedEncodingException e) {
			}
			return null;
		}

		/**
		 */
		private void init(final byte[] mSalt) {
			if (mSalt != null) {
				salt = mSalt;
			}
			if (ecipher == null) {
				try {
					final KeySpec keySpec = new PBEKeySpec(passPhrase, salt, iterationCount);
					final SecretKey key = SecretKeyFactory.getInstance(mAlgorithm).generateSecret(
					    keySpec);
					ecipher = Cipher.getInstance(mAlgorithm);
					dcipher = Cipher.getInstance(mAlgorithm);
					final AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt,
					    iterationCount);
					ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
					dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
				}
				catch (final java.security.InvalidAlgorithmParameterException e) {
				}
				catch (final java.security.spec.InvalidKeySpecException e) {
				}
				catch (final javax.crypto.NoSuchPaddingException e) {
				}
				catch (final java.security.NoSuchAlgorithmException e) {
				}
				catch (final java.security.InvalidKeyException e) {
				}
			}
		}
	}

	static public class IntHolder {
		private int value;

		public IntHolder() {
		}

		public IntHolder(final int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public void setValue(final int value) {
			this.value = value;
		}

		public String toString() {
			return new String("IntHolder(") + value + ")";
		}
	}

	public static class ObjectHolder {
		Object object;

		public ObjectHolder() {
		}

		public Object getObject() {
			return object;
		}

		public void setObject(final Object object) {
			this.object = object;
		}
	}

	public static class Pair {
		Object first;
		Object second;

		public Pair(final Object first, final Object second) {
			this.first = first;
			this.second = second;
		}

		public Object getFirst() {
			return first;
		}

		public Object getSecond() {
			return second;
		}
	}

	public static class SingleDesEncrypter extends DesEncrypter {
		public SingleDesEncrypter(final StringBuffer pPassPhrase) {
			super(pPassPhrase, "PBEWithMD5AndDES");
		}
	}

	public static class TripleDesEncrypter extends DesEncrypter {
		public TripleDesEncrypter(final StringBuffer pPassPhrase) {
			super(pPassPhrase, "PBEWithMD5AndTripleDES");
		}
	}

	private static Set availableFontFamilyNames = null;
	public static final Set executableExtensions = new HashSet(Arrays.asList(new String[] { "exe",
	        "com", "vbs", "bat", "lnk" }));
	public static final String JAVA_VERSION = System.getProperty("java.version");
	private static String sEnvFonts[] = null;

	public static void addEscapeActionToDialog(final JDialog dialog) {
		class EscapeAction extends AbstractAction {
			public void actionPerformed(final ActionEvent e) {
				dialog.dispose();
			};
		}
		Tools.addEscapeActionToDialog(dialog, new EscapeAction());
	}

	public static void addEscapeActionToDialog(final JDialog dialog, final Action action) {
		Tools.addKeyActionToDialog(dialog, action, "ESCAPE", "end_dialog");
	}

	public static void addKeyActionToDialog(final JDialog dialog, final Action action,
	                                        final String keyStroke, final String actionId) {
		action.putValue(Action.NAME, actionId);
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		    KeyStroke.getKeyStroke(keyStroke), action.getValue(Action.NAME));
		dialog.getRootPane().getActionMap().put(action.getValue(Action.NAME), action);
	}

	public static String BooleanToXml(final boolean col) {
		return (col) ? "true" : "false";
	}

	/**
	 */
	public static String byteArrayToUTF8String(final byte[] compressedData) {
		try {
			return new String(compressedData, "UTF8");
		}
		catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("UTF8 packing not allowed");
		}
	}

	public static String colorToXml(final Color col) {
		if (col == null) {
			return null;
		}
		String red = Integer.toHexString(col.getRed());
		if (col.getRed() < 16) {
			red = "0" + red;
		}
		String green = Integer.toHexString(col.getGreen());
		if (col.getGreen() < 16) {
			green = "0" + green;
		}
		String blue = Integer.toHexString(col.getBlue());
		if (col.getBlue() < 16) {
			blue = "0" + blue;
		}
		return "#" + red + green + blue;
	}

	public static String compress(final String message) {
		final byte[] input = Tools.uTF8StringToByteArray(message);
		final Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_COMPRESSION);
		compressor.setInput(input);
		compressor.finish();
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
		final byte[] buf = new byte[1024];
		while (!compressor.finished()) {
			final int count = compressor.deflate(buf);
			bos.write(buf, 0, count);
		}
		try {
			bos.close();
		}
		catch (final IOException e) {
		}
		final byte[] compressedData = bos.toByteArray();
		return Tools.toBase64(compressedData);
	}

	public static void convertPointFromAncestor(final Component source, final Point p, Component c) {
		int x, y;
		while (c != source) {
			x = c.getX();
			y = c.getY();
			p.x -= x;
			p.y -= y;
			c = c.getParent();
		};
	}

	public static void convertPointToAncestor(final Component source, final Point point,
	                                          final Class ancestorClass) {
		final Component destination = SwingUtilities.getAncestorOfClass(ancestorClass, source);
		Tools.convertPointToAncestor(source, point, destination);
	}

	public static void convertPointToAncestor(Component c, final Point p,
	                                          final Component destination) {
		int x, y;
		while (c != destination) {
			x = c.getX();
			y = c.getY();
			p.x += x;
			p.y += y;
			c = c.getParent();
		};
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

	public static String dateToString(final Date date) {
		return Long.toString(date.getTime());
	}

	public static String decompress(final String compressedMessage) {
		final byte[] compressedData = Tools.fromBase64(compressedMessage);
		final Inflater decompressor = new Inflater();
		decompressor.setInput(compressedData);
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedData.length);
		final byte[] buf = new byte[1024];
		while (!decompressor.finished()) {
			try {
				final int count = decompressor.inflate(buf);
				bos.write(buf, 0, count);
			}
			catch (final DataFormatException e) {
			}
		}
		try {
			bos.close();
		}
		catch (final IOException e) {
		}
		final byte[] decompressedData = bos.toByteArray();
		return Tools.byteArrayToUTF8String(decompressedData);
	}

	public static boolean executableByExtension(final String file) {
		return Tools.executableExtensions.contains(Tools.getExtension(file));
	}

	/**
	 * Replaces a ~ in a filename with the users home directory
	 */
	public static String expandFileName(String file) {
		if (file.startsWith("~")) {
			file = System.getProperty("user.home") + file.substring(1);
		}
		return file;
	}

	/**
	 * Example: expandPlaceholders("Hello $1.","Dolly"); => "Hello Dolly."
	 */
	public static String expandPlaceholders(final String message, String s1) {
		String result = message;
		if (s1 != null) {
			s1 = s1.replaceAll("\\\\", "\\\\\\\\");
			result = result.replaceAll("\\$1", s1);
		}
		return result;
	}

	public static String expandPlaceholders(final String message, final String s1, final String s2) {
		String result = message;
		if (s1 != null) {
			result = result.replaceAll("\\$1", s1);
		}
		if (s2 != null) {
			result = result.replaceAll("\\$2", s2);
		}
		return result;
	}

	public static String expandPlaceholders(final String message, final String s1, final String s2,
	                                        final String s3) {
		String result = message;
		if (s1 != null) {
			result = result.replaceAll("\\$1", s1);
		}
		if (s2 != null) {
			result = result.replaceAll("\\$2", s2);
		}
		if (s3 != null) {
			result = result.replaceAll("\\$3", s3);
		}
		return result;
	}

	public static URL fileToUrl(final File pFile) throws MalformedURLException {
		if (Tools.JAVA_VERSION.compareTo("1.6.0") < 0) {
			return pFile.toURL();
		}
		return pFile.toURI().toURL();
	}

	public static String firstLetterCapitalized(final String text) {
		if (text == null || text.length() == 0) {
			return text;
		}
		return text.substring(0, 1).toUpperCase() + text.substring(1, text.length());
	}

	/**
	 * @throws IOException
	 */
	public static byte[] fromBase64(final String base64String) {
		return Base64Coding.decode64(base64String);
	}

	/**
	 * Creates a default reader that just reads the given file.
	 *
	 * @throws FileNotFoundException
	 */
	public static Reader getActualReader(final File file) throws FileNotFoundException {
		return new BufferedReader(new FileReader(file));
	}

	public static Set getAvailableFontFamilyNames() {
		if (Tools.availableFontFamilyNames == null) {
			final String[] envFonts = Tools.getAvailableFonts();
			Tools.availableFontFamilyNames = new HashSet();
			for (int i = 0; i < envFonts.length; i++) {
				Tools.availableFontFamilyNames.add(envFonts[i]);
			}
			Tools.availableFontFamilyNames.add("dialog");
		}
		return Tools.availableFontFamilyNames;
	}

	public static Vector getAvailableFontFamilyNamesAsVector() {
		final String[] envFonts = Tools.getAvailableFonts();
		final Vector availableFontFamilyNames = new Vector();
		for (int i = 0; i < envFonts.length; i++) {
			availableFontFamilyNames.add(envFonts[i]);
		}
		return availableFontFamilyNames;
	}

	/**
	 */
	private static String[] getAvailableFonts() {
		if (Tools.sEnvFonts == null) {
			final GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
			Tools.sEnvFonts = gEnv.getAvailableFontFamilyNames();
		}
		return Tools.sEnvFonts;
	}

	/**
	 * Returns the lowercase of the extension of a file.
	 */
	public static String getExtension(final File f) {
		return Tools.getExtension(f.toString());
	}

	/**
	 * Returns the lowercase of the extension of a file.
	 */
	public static String getExtension(final String s) {
		final int i = s.lastIndexOf('.');
		return (i > 0 && i < s.length() - 1) ? s.substring(i + 1).toLowerCase().trim() : "";
	}

	/**
	 * In case of trouble, the method returns null.
	 *
	 * @param pInputFile
	 *            the file to read.
	 * @return the complete content of the file. or null if an exception has
	 *         occured.
	 */
	public static String getFile(final File pInputFile) {
		final StringBuffer lines = new StringBuffer();
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
			org.freeplane.main.Tools.logException(e);
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				}
				catch (final Exception ex) {
					org.freeplane.main.Tools.logException(ex);
				}
			}
			return null;
		}
		return lines.toString();
	}

	public static KeyStroke getKeyStroke(final String keyStrokeDescription) {
		if (keyStrokeDescription == null) {
			return null;
		}
		final KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeDescription);
		if (keyStroke != null) {
			return keyStroke;
		}
		return KeyStroke.getKeyStroke("typed " + keyStrokeDescription);
	}

	/**
	 * Creates a reader that pipes the input file through a XSLT-Script that
	 * updates the version to the current.
	 *
	 * @throws IOException
	 */
	public static Reader getUpdateReader(final File file, final String xsltScript)
	        throws IOException {
		StringWriter writer = null;
		InputStream inputStream = null;
		boolean successful = false;
		try {
			URL updaterUrl = null;
			updaterUrl = Controller.getResourceController().getResource(xsltScript);
			if (updaterUrl == null) {
				throw new IllegalArgumentException(xsltScript + " not found.");
			}
			inputStream = updaterUrl.openStream();
			final Source xsltSource = new StreamSource(inputStream);
			writer = new StringWriter();
			final Result result = new StreamResult(writer);
			class TransformerRunnable implements Runnable {
				private boolean successful = false;

				public boolean isSuccessful() {
					return successful;
				}

				public void run() {
					final TransformerFactory transFact = TransformerFactory.newInstance();
					Transformer trans;
					try {
						trans = transFact.newTransformer(xsltSource);
						trans.transform(new StreamSource(file), result);
						successful = true;
					}
					catch (final Exception ex) {
						org.freeplane.main.Tools.logException(ex);
					}
				}
			}
			final TransformerRunnable transformer = new TransformerRunnable();
			final Thread transformerThread = new Thread(transformer, "XSLT");
			transformerThread.start();
			transformerThread.join();
			successful = transformer.isSuccessful();
		}
		catch (final Exception ex) {
		}
		finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (writer != null) {
				writer.close();
			}
		}
		if (successful) {
			return new StringReader(writer.getBuffer().toString());
		}
		else {
			return Tools.getActualReader(file);
		}
	}

	/**
	 * Returns the same URL as input with the addition, that the reference part
	 * "#..." is filtered out.
	 *
	 * @throws MalformedURLException
	 */
	public static URL getURLWithoutReference(final URL input) throws MalformedURLException {
		return new URL(input.toString().replaceFirst("#.*", ""));
	}

	public static boolean isAbsolutePath(final String path) {
		final String osNameStart = System.getProperty("os.name").substring(0, 3);
		final String fileSeparator = System.getProperty("file.separator");
		if (osNameStart.equals("Win")) {
			return ((path.length() > 1) && path.substring(1, 2).equals(":"))
			        || path.startsWith(fileSeparator);
		}
		else if (osNameStart.equals("Mac")) {
			return path.startsWith(fileSeparator);
		}
		else {
			return path.startsWith(fileSeparator);
		}
	}

	public static boolean isAvailableFontFamily(final String fontFamilyName) {
		return Tools.getAvailableFontFamilyNames().contains(fontFamilyName);
	}

	public static boolean isMacOsX() {
		boolean underMac = false;
		final String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) {
			underMac = true;
		}
		return underMac;
	}

	/**
	 * Tests a string to be equals with "true".
	 *
	 * @return true, iff the String is "true".
	 */
	public static boolean isPreferenceTrue(final String option) {
		return Tools.safeEquals(option, "true");
	}

	public static String listToString(final List list) {
		final ListIterator it = list.listIterator(0);
		String str = new String();
		while (it.hasNext()) {
			str += it.next().toString() + ";";
		}
		return str;
	}

	public static void logException(final Throwable e) {
		Tools.logException(e, "");
	}

	public static void logException(final Throwable e, final String comment) {
		Logger.global.log(Level.SEVERE, "An exception occured: " + comment, e);
	}

	public static void logTransferable(final Transferable t) {
		System.err.println();
		System.err.println("BEGIN OF Transferable:\t" + t);
		final DataFlavor[] dataFlavors = t.getTransferDataFlavors();
		for (int i = 0; i < dataFlavors.length; i++) {
			System.out.println("  Flavor:\t" + dataFlavors[i]);
			System.out.println("    Supported:\t" + t.isDataFlavorSupported(dataFlavors[i]));
			try {
				System.out.println("    Content:\t" + t.getTransferData(dataFlavors[i]));
			}
			catch (final Exception e) {
			}
		}
		System.err.println("END OF Transferable");
		System.err.println();
	}

	/** \0 is not allowed: */
	public static String makeValidXml(final String pXmlNoteText) {
		return pXmlNoteText.replace('\0', ' ');
	}

	public static String PointToXml(final Point col) {
		if (col == null) {
			return null;
		}
		final Vector l = new Vector();
		l.add(Integer.toString(col.x));
		l.add(Integer.toString(col.y));
		return Tools.listToString(l);
	}

	public static String removeExtension(final String s) {
		final int i = s.lastIndexOf('.');
		return (i > 0 && i < s.length() - 1) ? s.substring(0, i) : "";
	}

	public static String removeMnemonic(final String rawLabel) {
		return rawLabel.replaceFirst("&([^ ])", "$1");
	}

	public static ListIterator resetIterator(final ListIterator iterator) {
		while (iterator.hasPrevious()) {
			iterator.previous();
		}
		return iterator;
	}

	public static boolean safeEquals(final BooleanHolder holder, final BooleanHolder holder2) {
		return (holder == null && holder2 == null)
		        || (holder != null && holder2 != null && holder.getValue() == holder2.getValue());
	}

	public static boolean safeEquals(final Color color1, final Color color2) {
		return (color1 != null && color2 != null && color1.equals(color2))
		        || (color1 == null && color2 == null);
	}

	public static boolean safeEquals(final Object obj1, final Object obj2) {
		return (obj1 != null && obj2 != null && obj1.equals(obj2))
		        || (obj1 == null && obj2 == null);
	}

	public static boolean safeEquals(final String string1, final String string2) {
		return (string1 != null && string2 != null && string1.equals(string2))
		        || (string1 == null && string2 == null);
	}

	public static boolean safeEqualsIgnoreCase(final String string1, final String string2) {
		return (string1 != null && string2 != null && string1.toLowerCase().equals(
		    string2.toLowerCase()))
		        || (string1 == null && string2 == null);
	}

	public static void setDialogLocationRelativeTo(final JDialog dialog, Component c) {
		if (c == null) {
			return;
		}
		if (c instanceof NodeView) {
			final NodeView nodeView = (NodeView) c;
			nodeView.getMap().scrollNodeToVisible(nodeView);
			c = nodeView.getMainView();
		}
		final Point compLocation = c.getLocationOnScreen();
		final int cw = c.getWidth();
		final int ch = c.getHeight();
		final Container parent = dialog.getParent();
		final Point parentLocation = parent.getLocationOnScreen();
		final int pw = parent.getWidth();
		final int ph = parent.getHeight();
		final int dw = dialog.getWidth();
		final int dh = dialog.getHeight();
		final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
		final Insets screenInsets = defaultToolkit.getScreenInsets(dialog
		    .getGraphicsConfiguration());
		final Dimension screenSize = defaultToolkit.getScreenSize();
		final int minX = Math.max(parentLocation.x, screenInsets.left);
		final int minY = Math.max(parentLocation.y, screenInsets.top);
		final int maxX = Math.min(parentLocation.x + pw, screenSize.width - screenInsets.right);
		final int maxY = Math.min(parentLocation.y + ph, screenSize.height - screenInsets.bottom);
		int dx, dy;
		if (compLocation.x + cw < minX) {
			dx = minX;
		}
		else if (compLocation.x > maxX) {
			dx = maxX - dw;
		}
		else {
			final int leftSpace = compLocation.x - minX;
			final int rightSpace = maxX - (compLocation.x + cw);
			if (leftSpace > rightSpace) {
				if (leftSpace > dw) {
					dx = compLocation.x - dw;
				}
				else {
					dx = minX;
				}
			}
			else {
				if (rightSpace > dw) {
					dx = compLocation.x + cw;
				}
				else {
					dx = maxX - dw;
				}
			}
		}
		if (compLocation.y + ch < minY) {
			dy = minY;
		}
		else if (compLocation.y > maxY) {
			dy = maxY - dh;
		}
		else {
			final int topSpace = compLocation.y - minY;
			final int bottomSpace = maxY - (compLocation.y + ch);
			if (topSpace > bottomSpace) {
				if (topSpace > dh) {
					dy = compLocation.y - dh;
				}
				else {
					dy = minY;
				}
			}
			else {
				if (bottomSpace > dh) {
					dy = compLocation.y + ch;
				}
				else {
					dy = maxY - dh;
				}
			}
		}
		dialog.setLocation(dx, dy);
	}

	public static void setHidden(final File file, final boolean hidden, final boolean synchronously) {
		final String osNameStart = System.getProperty("os.name").substring(0, 3);
		if (osNameStart.equals("Win")) {
			try {
				Runtime.getRuntime().exec(
				    "attrib " + (hidden ? "+" : "-") + "H \"" + file.getAbsolutePath() + "\"");
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
				org.freeplane.main.Tools.logException(e);
			}
		}
	}

	/**
	 * Converts a String in the format "value;value;value" to a List with the
	 * values (as strings)
	 */
	public static List stringToList(final String string) {
		final StringTokenizer tok = new StringTokenizer(string, ";");
		final List list = new LinkedList();
		while (tok.hasMoreTokens()) {
			list.add(tok.nextToken());
		}
		return list;
	}

	/**
	 */
	public static String toBase64(final byte[] byteBuffer) {
		return new String(Base64Coding.encode64(byteBuffer));
	}

	/**
	 * This method converts an absolute url to an url relative to a given
	 * base-url. The algorithm is somewhat chaotic, but it works (Maybe rewrite
	 * it). Be careful, the method is ".mm"-specific. Something like this should
	 * be included in the librarys, but I couldn't find it. You can create a new
	 * absolute url with "new URL(URL context, URL relative)".
	 */
	public static String toRelativeURL(final URL base, final URL target) {
		if ((base.getProtocol().equals(target.getProtocol()))
		        && (base.getHost().equals(target.getHost()))) {
			String baseString = base.getFile();
			String targetString = target.getFile();
			String result = "";
			baseString = baseString.substring(0, baseString.lastIndexOf("/") + 1);
			targetString = targetString.substring(0, targetString.lastIndexOf("/") + 1);
			final StringTokenizer baseTokens = new StringTokenizer(baseString, "/");
			final StringTokenizer targetTokens = new StringTokenizer(targetString, "/");
			String nextBaseToken = "", nextTargetToken = "";
			while (baseTokens.hasMoreTokens() && targetTokens.hasMoreTokens()) {
				nextBaseToken = baseTokens.nextToken();
				nextTargetToken = targetTokens.nextToken();
				if (!(nextBaseToken.equals(nextTargetToken))) {
					while (true) {
						result = result.concat("../");
						if (!baseTokens.hasMoreTokens()) {
							break;
						}
						nextBaseToken = baseTokens.nextToken();
					}
					while (true) {
						result = result.concat(nextTargetToken + "/");
						if (!targetTokens.hasMoreTokens()) {
							break;
						}
						nextTargetToken = targetTokens.nextToken();
					}
					final String temp = target.getFile();
					result = result
					    .concat(temp.substring(temp.lastIndexOf("/") + 1, temp.length()));
					return result;
				}
			}
			while (baseTokens.hasMoreTokens()) {
				result = result.concat("../");
				baseTokens.nextToken();
			}
			while (targetTokens.hasMoreTokens()) {
				nextTargetToken = targetTokens.nextToken();
				result = result.concat(nextTargetToken + "/");
			}
			final String temp = target.getFile();
			result = result.concat(temp.substring(temp.lastIndexOf("/") + 1, temp.length()));
			return result;
		}
		return target.toString();
	}

	/**
	 * This is a correction of a method getFile of a class URL. Namely, on
	 * Windows it returned file paths like /C: etc., which are not valid on
	 * Windows. This correction is heuristic to a great extend. One of the
	 * reasons is that file: something every browser and every system uses
	 * slightly differently.
	 */
	public static String urlGetFile(final URL url) {
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
		if (Tools.JAVA_VERSION.compareTo("1.6.0") < 0) {
			return new File(Tools.urlGetFile(pUrl));
		}
		return new File(new URI(pUrl.toString()));
	}

	/**
	 */
	public static byte[] uTF8StringToByteArray(final String uncompressedData) {
		try {
			return uncompressedData.getBytes("UTF8");
		}
		catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("UTF8 packing not allowed");
		}
	}

	public static boolean xmlToBoolean(final String string) {
		if (string == null) {
			return false;
		}
		if (string.equals("true")) {
			return true;
		}
		return false;
	}

	public static Color xmlToColor(String string) {
		if (string == null) {
			return null;
		}
		string = string.trim();
		if (string.length() == 7) {
			final int red = Integer.parseInt(string.substring(1, 3), 16);
			final int green = Integer.parseInt(string.substring(3, 5), 16);
			final int blue = Integer.parseInt(string.substring(5, 7), 16);
			return new Color(red, green, blue);
		}
		else {
			throw new IllegalArgumentException("No xml color given by '" + string + "'.");
		}
	}

	/**
	 * Extracts a long from xml. Only useful for dates.
	 */
	public static Date xmlToDate(final String xmlString) {
		try {
			return new Date(Long.valueOf(xmlString).longValue());
		}
		catch (final Exception e) {
			return new Date(System.currentTimeMillis());
		}
	}

	public static Point xmlToPoint(String string) {
		if (string == null) {
			return null;
		}
		if (string.startsWith("java.awt.Point")) {
			string = string.replaceAll("java\\.awt\\.Point\\[x=([0-9]*),y=([0-9]*)\\]", "$1;$2");
		}
		final List l = Tools.stringToList(string);
		final ListIterator it = l.listIterator(0);
		if (l.size() != 2) {
			throw new IllegalArgumentException("A point must consist of two numbers (and not: '"
			        + string + "').");
		}
		final int x = Integer.parseInt((String) it.next());
		final int y = Integer.parseInt((String) it.next());
		return new Point(x, y);
	}
}
