package org.freeplane.n3.nanoxml;

import java.io.OutputStream;
import java.io.Writer;

/**
 * An RawXMLWriter writes XML data to a stream. In contrast to RawXMLWriter the RawXMLWriter uses CDATA sections to protect
 * the content of a node.
 * 
 * @see org.freeplane.n3.nanoxml.XMLWriter
 */
public class CdataContentXmlWriter extends XMLWriter {
	public CdataContentXmlWriter(OutputStream stream) {
		super(stream);
	}

	public CdataContentXmlWriter(final Writer writer) {
		super(writer);
	}

	/**
	 * Writes a string encoding reserved characters.
	 * 
	 * @param str
	 *            the string to write.
	 */
	protected void writeEncodedContent(final String str) {
		getWriter().print("<![CDATA[" + str + "]]>");
	}
}
