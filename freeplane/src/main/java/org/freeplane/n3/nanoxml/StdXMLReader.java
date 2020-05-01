/*
 * StdXMLReader.java NanoXML/Java $Revision: 1.4 $ $Date: 2002/01/04 21:03:28 $
 * $Name: RELEASE_2_2_1 $ This file is part of NanoXML 2 for Java. Copyright (C)
 * 2000-2002 Marc De Scheemaecker, All Rights Reserved. This software is
 * provided 'as-is', without any express or implied warranty. In no event will
 * the authors be held liable for any damages arising from the use of this
 * software. Permission is granted to anyone to use this software for any
 * purpose, including commercial applications, and to alter it and redistribute
 * it freely, subject to the following restrictions: 1. The origin of this
 * software must not be misrepresented; you must not claim that you wrote the
 * original software. If you use this software in a product, an acknowledgment
 * in the product documentation would be appreciated but is not required. 2.
 * Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software. 3. This notice may not be
 * removed or altered from any source distribution.
 */
package org.freeplane.n3.nanoxml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PushbackInputStream;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;

/**
 * StdXMLReader reads the data to be parsed.
 * 
 * @author Marc De Scheemaecker
 * Modified by Dimitry Polivaev (2008)
 */
public class StdXMLReader implements IXMLReader {
	/**
	 * A stacked reader.
	 * 
	 * @author Marc De Scheemaecker
	 * Modified by Dimitry Polivaev (2008)
	 */
	private class StackedReader {
		LineNumberReader lineReader;
		Reader pbReader;
		String publicId;
		URL systemId;
	}

	/**
	 * Creates a new reader using a file as input.
	 * 
	 * @param filename
	 *            the name of the file containing the XML data
	 * @throws java.io.FileNotFoundException
	 *             if the file could not be found
	 * @throws java.io.IOException
	 *             if an I/O error occurred
	 */
	public static IXMLReader fileReader(final String filename) throws FileNotFoundException, IOException {
		final StdXMLReader r = new StdXMLReader(new FileInputStream(filename));
		r.setSystemID(filename);
		for (int i = 0; i < r.readers.size(); i++) {
			final StackedReader sr = (StackedReader) r.readers.elementAt(i);
			sr.systemId = r.currentReader.systemId;
		}
		return r;
	}

	/**
	 * Creates a new reader using a string as input.
	 * 
	 * @param str
	 *            the string containing the XML data
	 */
	public static IXMLReader stringReader(final String str) {
		return new StdXMLReader(new StringReader(str));
	}

	private char charReadTooMuch;
	/**
	 * The current push-back reader.
	 */
	private StackedReader currentReader;
	/**
	 * The stack of readers.
	 */
	final private Stack<StackedReader> readers;

	/**
	 * Initializes the XML reader.
	 * 
	 * @param stream
	 *            the input for the XML data.
	 * @throws java.io.IOException
	 *             if an I/O error occurred
	 */
	public StdXMLReader(final InputStream stream) throws IOException {
		final StringBuilder charsRead = new StringBuilder();
		final Reader reader = this.stream2reader(stream, charsRead);
		currentReader = new StackedReader();
		readers = new Stack<StackedReader>();
		currentReader.lineReader = new LineNumberReader(reader);
		currentReader.pbReader = currentReader.lineReader;
		currentReader.publicId = "";
		charReadTooMuch = '\0';
		try {
			currentReader.systemId = new URL("file:.");
		}
		catch (final MalformedURLException e) {
		}
		this.startNewStream(new StringReader(charsRead.toString()));
	}

	/**
	 * Initializes the XML reader.
	 * 
	 * @param reader
	 *            the input for the XML data.
	 */
	public StdXMLReader(final Reader reader) {
		currentReader = new StackedReader();
		readers = new Stack<StackedReader>();
		currentReader.lineReader = new LineNumberReader(reader);
		currentReader.pbReader = currentReader.lineReader;
		currentReader.publicId = "";
		charReadTooMuch = '\0';
		try {
			currentReader.systemId = new URL("file:.");
		}
		catch (final MalformedURLException e) {
		}
	}

	/**
	 * Initializes the reader from a system and public ID.
	 * 
	 * @param publicID
	 *            the public ID which may be null.
	 * @param systemID
	 *            the non-null system ID.
	 * @throws MalformedURLException
	 *             if the system ID does not contain a valid URL
	 * @throws FileNotFoundException
	 *             if the system ID refers to a local file which does not exist
	 * @throws IOException
	 *             if an error occurred opening the stream
	 */
	public StdXMLReader(final String publicID, String systemID) throws MalformedURLException, FileNotFoundException,
	        IOException {
		URL systemIDasURL = null;
		charReadTooMuch = '\0';
		try {
			systemIDasURL = new URL(systemID);
		}
		catch (final MalformedURLException e) {
			systemID = "file:" + systemID;
			try {
				systemIDasURL = new URL(systemID);
			}
			catch (final MalformedURLException e2) {
				throw e;
			}
		}
		currentReader = new StackedReader();
		readers = new Stack<StackedReader>();
		final Reader reader = this.openStream(publicID, systemIDasURL.toString());
		currentReader.lineReader = new LineNumberReader(reader);
		currentReader.pbReader = currentReader.lineReader;
	}

	/**
	 * Returns true if there are no more characters left to be read.
	 * 
	 * @throws java.io.IOException
	 *             if an I/O error occurred
	 */
	public boolean atEOF() throws IOException {
		int ch = readImpl();
		while (ch < 0) {
			if (readers.empty()) {
				return true;
			}
			currentReader.pbReader.close();
			currentReader = (StackedReader) readers.pop();
			ch = readImpl();
		}
		unread(ch);
		return false;
	}

	/**
	 * Returns true if the current stream has no more characters left to be
	 * read.
	 * 
	 * @throws java.io.IOException
	 *             if an I/O error occurred
	 */
	public boolean atEOFOfCurrentStream() throws IOException {
		final int ch = readImpl();
		if (ch < 0) {
			return true;
		}
		else {
			unread(ch);
			return false;
		}
	}

	/**
	 * Cleans up the object when it's destroyed.
	 */
	@Override
	protected void finalize() throws Throwable {
		currentReader.lineReader = null;
		currentReader.pbReader = null;
		currentReader.systemId = null;
		currentReader.publicId = null;
		currentReader = null;
		readers.clear();
		super.finalize();
	}

	/**
	 * Scans the encoding from an &lt;?xml...?&gt; tag.
	 * 
	 * @param str
	 *            the first tag in the XML data.
	 * @return the encoding, or null if no encoding has been specified.
	 */
	protected String getEncoding(final String str) {
		if (!str.startsWith("<?xml")) {
			return null;
		}
		int index = 5;
		while (index < str.length()) {
			final StringBuilder key = new StringBuilder();
			while ((index < str.length()) && (str.charAt(index) <= ' ')) {
				index++;
			}
			while ((index < str.length()) && (str.charAt(index) >= 'a') && (str.charAt(index) <= 'z')) {
				key.append(str.charAt(index));
				index++;
			}
			while ((index < str.length()) && (str.charAt(index) <= ' ')) {
				index++;
			}
			if ((index >= str.length()) || (str.charAt(index) != '=')) {
				break;
			}
			while ((index < str.length()) && (str.charAt(index) != '\'') && (str.charAt(index) != '"')) {
				index++;
			}
			if (index >= str.length()) {
				break;
			}
			final char delimiter = str.charAt(index);
			index++;
			final int index2 = str.indexOf(delimiter, index);
			if (index2 < 0) {
				break;
			}
			if (key.toString().equals("encoding")) {
				return str.substring(index, index2);
			}
			index = index2 + 1;
		}
		return null;
	}

	/**
	 * Returns the line number of the data in the current stream.
	 */
	public int getLineNr() {
		if (currentReader.lineReader == null) {
			final StackedReader sr = (StackedReader) readers.peek();
			if (sr.lineReader == null) {
				return 0;
			}
			else {
				return sr.lineReader.getLineNumber() + 1;
			}
		}
		return currentReader.lineReader.getLineNumber() + 1;
	}

	/**
	 * Returns the current public ID.
	 */
	public String getPublicID() {
		return currentReader.publicId;
	}

	/**
	 * Returns the current "level" of the stream on the stack of streams.
	 */
	public int getStreamLevel() {
		return readers.size();
	}

	/**
	 * Returns the current system ID.
	 */
	public String getSystemID() {
		return currentReader.systemId.toString();
	}

	/**
	 * Opens a stream from a public and system ID.
	 * 
	 * @param publicID
	 *            the public ID, which may be null
	 * @param systemID
	 *            the system ID, which is never null
	 * @throws java.net.MalformedURLException
	 *             if the system ID does not contain a valid URL
	 * @throws java.io.FileNotFoundException
	 *             if the system ID refers to a local file which does not exist
	 * @throws java.io.IOException
	 *             if an error occurred opening the stream
	 */
	public Reader openStream(final String publicID, final String systemID) throws MalformedURLException,
	        FileNotFoundException, IOException {
		URL url = new URL(currentReader.systemId, systemID);
		if (url.getRef() != null) {
			final String ref = url.getRef();
			if (url.getFile().length() > 0) {
				url = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
				url = new URL("jar:" + url + '!' + ref);
			}
			else {
				url = StdXMLReader.class.getResource(ref);
			}
		}
		currentReader.publicId = publicID;
		currentReader.systemId = url;
		final StringBuilder charsRead = new StringBuilder();
		final Reader reader = this.stream2reader(url.openStream(), charsRead);
		if (charsRead.length() == 0) {
			return reader;
		}
		final String charsReadStr = charsRead.toString();
		final PushbackReader pbreader = new PushbackReader(reader, charsReadStr.length());
		for (int i = charsReadStr.length() - 1; i >= 0; i--) {
			pbreader.unread(charsReadStr.charAt(i));
		}
		return pbreader;
	}

	/**
	 * Reads a character.
	 * 
	 * @return the character
	 * @throws java.io.IOException
	 *             if no character could be read
	 */
	public char read() throws IOException {
		int ch = readImpl();
		while (ch < 0) {
			if (readers.empty()) {
				throw new IOException("Unexpected EOF at line " + getLineNr());
			}
			currentReader.pbReader.close();
			currentReader = (StackedReader) readers.pop();
			ch = readImpl();
		}
		return (char) ch;
	}

	private int readImpl() throws IOException {
		if (charReadTooMuch != '\0') {
			final char ch = charReadTooMuch;
			charReadTooMuch = '\0';
			return ch;
		}
		final int ch = currentReader.pbReader.read();
		return ch;
	}

	/**
	 * Sets the public ID of the current stream.
	 * 
	 * @param publicID
	 *            the public ID
	 */
	public void setPublicID(final String publicID) {
		currentReader.publicId = publicID;
	}

	/**
	 * Sets the system ID of the current stream.
	 * 
	 * @param systemID
	 *            the system ID
	 * @throws java.net.MalformedURLException
	 *             if the system ID does not contain a valid URL
	 */
	public void setSystemID(final String systemID) throws MalformedURLException {
		currentReader.systemId = new URL(currentReader.systemId, systemID);
	}

	/**
	 * Starts a new stream from a Java reader. The new stream is used temporary
	 * to read data from. If that stream is exhausted, control returns to the
	 * parent stream.
	 * 
	 * @param reader
	 *            the non-null reader to read the new data from
	 */
	public void startNewStream(final Reader reader) {
		this.startNewStream(reader, false);
	}

	/**
	 * Starts a new stream from a Java reader. The new stream is used temporary
	 * to read data from. If that stream is exhausted, control returns to the
	 * parent stream.
	 * 
	 * @param reader
	 *            the non-null reader to read the new data from
	 * @param isInternalEntity
	 *            true if the reader is produced by resolving an internal entity
	 */
	public void startNewStream(final Reader reader, final boolean isInternalEntity) {
		final StackedReader oldReader = currentReader;
		readers.push(currentReader);
		currentReader = new StackedReader();
		if (isInternalEntity) {
			currentReader.lineReader = null;
			currentReader.pbReader = reader;
		}
		else {
			currentReader.lineReader = new LineNumberReader(reader);
			currentReader.pbReader = new PushbackReader(currentReader.lineReader, 2);
		}
		currentReader.systemId = oldReader.systemId;
		currentReader.publicId = oldReader.publicId;
	}

	/**
	 * Converts a stream to a reader while detecting the encoding.
	 * 
	 * @param stream
	 *            the input for the XML data.
	 * @param charsRead
	 *            buffer where to put characters that have been read
	 * @throws java.io.IOException
	 *             if an I/O error occurred
	 */
	protected Reader stream2reader(final InputStream stream, final StringBuilder charsRead) throws IOException {
		final PushbackInputStream pbstream = new PushbackInputStream(stream);
		int b = pbstream.read();
		switch (b) {
			case 0x00:
			case 0xFE:
			case 0xFF:
				pbstream.unread(b);
				return new InputStreamReader(pbstream, "UTF-16");
			case 0xEF:
				for (int i = 0; i < 2; i++) {
					pbstream.read();
				}
				return new InputStreamReader(pbstream, "UTF-8");
			case 0x3C:
				b = pbstream.read();
				charsRead.append('<');
				while ((b > 0) && (b != 0x3E)) {
					charsRead.append((char) b);
					b = pbstream.read();
				}
				if (b > 0) {
					charsRead.append((char) b);
				}
				final String encoding = this.getEncoding(charsRead.toString());
				if (encoding == null) {
					return new InputStreamReader(pbstream, "UTF-8");
				}
				charsRead.setLength(0);
				try {
					return new InputStreamReader(pbstream, encoding);
				}
				catch (final UnsupportedEncodingException e) {
					return new InputStreamReader(pbstream, "UTF-8");
				}
			default:
				charsRead.append((char) b);
				return new InputStreamReader(pbstream, "UTF-8");
		}
	}

	public void unread(final char ch) throws IOException {
		charReadTooMuch = ch;
	}

	/**
	 * Pushes the last character read back to the stream.
	 * 
	 * @param ch
	 *            the character to push back.
	 * @throws java.io.IOException
	 *             if an I/O error occurred
	 */
	public void unread(final int ch) throws IOException {
		unread((char) ch);
	}
}
