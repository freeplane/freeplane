package org.freeplane.core.util;

/*
 * XHTMLWriter -- A simple XHTML document writer (C) 2004 Richard "Shred"
 * Koerber http://www.shredzone.net/ This is free software. You can modify and
 * use it at will.
 */
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.Option;

import com.lightdev.app.shtm.SHTMLWriter;

/**
 * Create a new XHTMLWriter which is able to write out a HTMLDocument as XHTML.
 * <p>
 * The result will be a valid XML file, but it is not granted that the file will
 * really be XHTML 1.0 transitional conformous. The basic purpose of this class
 * is to give an XSL processor access to plain HTML files.
 * 
 * @author Richard "Shred" Koerber
 */
class XHTMLWriter extends SHTMLWriter {
	/**
	 * This FilterWriter will convert the output of Swing's HTMLWriter to XHTML
	 * format. This is done by converting tags like &lt;br&gt; to
	 * &lt;br&nbsp;/&gt;. Also, special characters in tag attributes are
	 * escaped.
	 * <p>
	 * This filter relies on known flaws of the HTMLWriter. It is known to work
	 * with Java 1.4, but might not work with future Java releases.
	 */
	public static class XHTMLFilterWriter extends FilterWriter {
		private boolean insideTag = false;
		private boolean insideValue = false;

		/**
		 * Create a new XHTMLFilterWriter.
		 * 
		 * @param writer
		 *            Writer to write to
		 */
		public XHTMLFilterWriter(final Writer writer) {
			super(writer);
		}

		/**
		 * Write a char array to the Writer.
		 * 
		 * @param cbuf
		 *            Char array to be written
		 * @param off
		 *            Start offset within the array
		 * @param len
		 *            Number of chars to be written
		 */
		@Override
		public void write(final char[] cbuf, int off, int len) throws IOException {
			while (len-- > 0) {
				write(cbuf[off++]);
			}
		}

		/**
		 * Write a single char to the Writer.
		 * 
		 * @param c
		 *            Char to be written
		 */
		@Override
		public void write(final int c) throws IOException {
			if (insideValue) {
				if (c == '&') {
					super.write("&amp;", 0, 5);
					return;
				}
				else if (c == '<') {
					super.write("&lt;", 0, 4);
					return;
				}
				else if (c == '>') {
					super.write("&gt;", 0, 4);
					return;
				}
				else if (c == '"') {
					insideValue = false;
				}
			}
			else if (insideTag) {
				if (c == '"') {
					insideValue = true;
				}
				else if (c == '>') {
					insideTag = false;
				}
			}
			else if (c == '<') {
				insideTag = true;
			}
			super.write(c);
		}

		/**
		 * Write a String to the Writer.
		 * 
		 * @param str
		 *            String to be written
		 * @param off
		 *            Start offset within the String
		 * @param len
		 *            Number of chars to be written
		 */
		@Override
		public void write(final String str, final int off, final int len) throws IOException {
			write(str.toCharArray(), off, len);
		}
	}

	/**
	 * Read HTML from the Reader, and send XHTML to the writer. Common mistakes
	 * in the HTML code will also be corrected. The result is pretty-printed.
	 * 
	 * @param reader
	 *            HTML source
	 * @param writer
	 *            XHTML target
	 */
	public static void html2xhtml(final Reader reader, final Writer writer) throws IOException, BadLocationException {
		final HTMLEditorKit kit = new HTMLEditorKit();
		final Document doc = kit.createDefaultDocument();
		kit.read(reader, doc, doc.getLength());
		final XHTMLWriter xhw = new XHTMLWriter(writer, (HTMLDocument) doc);
		xhw.write();
	}

	/**
	 * External call to convert a source HTML file to a target XHTML file.
	 * <p>
	 * Usage: <tt>java XHTMLWriter &lt;source file&gt; &lt;target file&gt;</tt>
	 * 
	 * @param args
	 *            Shell arguments
	 */
	public static void main(final String[] args) {
		try {
			final FileReader reader = new FileReader(args[0]);
			final FileWriter writer = new FileWriter(args[1]);
			XHTMLWriter.html2xhtml(reader, writer);
			writer.close();
			reader.close();
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
	}

	private boolean writeLineSeparatorEnabled = true;
	private boolean insideEmptyTag;

	/**
	 * Create a new XHTMLWriter that will write the entire HTMLDocument.
	 * 
	 * @param writer
	 *            Writer to write to
	 * @param doc
	 *            Source document
	 */
	public XHTMLWriter(final Writer writer, final HTMLDocument doc) {
		this(writer, doc, 0, doc.getLength());
	}

	/**
	 * Create a new XHTMLWriter that will write a part of a HTMLDocument.
	 * 
	 * @param writer
	 *            Writer to write to
	 * @param doc
	 *            Source document
	 * @param pos
	 *            Starting position
	 * @param len
	 *            Length
	 */
	public XHTMLWriter(final Writer writer, final HTMLDocument doc, final int pos, final int len) {
		super(new XHTMLFilterWriter(writer), doc, pos, len);
		setLineLength(Integer.MAX_VALUE);
	}

	/**
	 * Start the writing process. An XML and DOCTYPE header will be written
	 * prior to the XHTML output.
	 */
	@Override
	public void write() throws IOException, BadLocationException {
		super.write();
	}

	@Override
	protected void writeLineSeparator() throws IOException {
		if (writeLineSeparatorEnabled) {
			super.writeLineSeparator();
		}
	}

	@Override
	protected void writeOption(final Option option) throws IOException {
		writeLineSeparatorEnabled = false;
		super.writeOption(option);
		writeLineSeparatorEnabled = true;
		write("</option>");
		writeLineSeparator();
	}

	/* (non-Javadoc)
	* @see javax.swing.text.html.HTMLWriter#writeAttributes(javax.swing.text.AttributeSet)
	*/
	@Override
	protected void writeAttributes(final AttributeSet attributeSet) throws IOException {
		final Object nameTag = (attributeSet != null) ? attributeSet.getAttribute(StyleConstants.NameAttribute) : null;
		final Object endTag = (attributeSet != null) ? attributeSet.getAttribute(HTML.Attribute.ENDTAG) : null;
		// write no attributes for end tags
		if (nameTag != null && endTag != null && (endTag instanceof String) && ((String) endTag).equals("true")) {
			return;
		}
		super.writeAttributes(attributeSet);
		if (insideEmptyTag) {
			write('/');
		}
	}

	// remove invalid characters
	@Override
	protected void output(final char[] chars, final int start, final int length) throws IOException {
		for (int i = start; i < start + length; i++) {
			final char c = chars[i];
			if (c < 32 && c != '\r' && c != '\n' && c != '\t') {
				chars[i] = ' ';
			}
		}
		super.output(chars, start, length);
	}

	@Override
	protected void emptyTag(final Element elem) throws BadLocationException, IOException {
		try {
			final boolean isEndtag = isEndtag(elem);
			final int balance = balance(elem, isEndtag);
			if (balance == 0 || balance > 0 && isEndtag || balance < 0 && !isEndtag) {
				super.emptyTag(elem);
				return;
			}
			if (isEndtag) {
				write('<');
				write(elem.getName());
				write('/');
				write('>');
				return;
			}
			insideEmptyTag = true;
			super.emptyTag(elem);
		}
		finally {
			insideEmptyTag = false;
		}
	}

	private int balance(final Element elem, final boolean isEndtag) {
		final Element parentElement = elem.getParentElement();
		final int elementCount = parentElement.getElementCount();
		int balance = 0;
		final String elemName = elem.getName();
		for (int i = 0; i < elementCount; i++) {
			final Element childElement = parentElement.getElement(i);
			if (isEndtag) {
				if (childElement.equals(elem)) {
					balance--;
					break;
				}
			}
			else {
				if (childElement.equals(elem)) {
					balance = 1;
					continue;
				}
				if (balance == 0) {
					continue;
				}
			}
			if (!elemName.equals(childElement.getName())) {
				continue;
			}
			if (isEndtag(childElement)) {
				if (balance > 0) {
					balance--;
					continue;
				}
			}
			else {
				balance++;
			}
		}
		return balance;
	}

	private boolean isEndtag(final Element elem) {
		final AttributeSet attributes = elem.getAttributes();
		final Object endTag = attributes.getAttribute(HTML.Attribute.ENDTAG);
		final boolean isEndtag = (endTag instanceof String) && ((String) endTag).equals("true");
		return isEndtag;
	}

	@Override
	protected void closeOutUnwantedEmbeddedTags(final AttributeSet attr) throws IOException {
		final boolean insideEmptyTag = this.insideEmptyTag;
		this.insideEmptyTag = false;
		try {
			super.closeOutUnwantedEmbeddedTags(attr);
		}
		finally {
			this.insideEmptyTag = insideEmptyTag;
		}
	}

	@Override
	protected void writeEmbeddedTags(final AttributeSet attr) throws IOException {
		final boolean insideEmptyTag = this.insideEmptyTag;
		this.insideEmptyTag = false;
		try {
			super.writeEmbeddedTags(attr);
		}
		finally {
			this.insideEmptyTag = insideEmptyTag;
		}
	}
}
