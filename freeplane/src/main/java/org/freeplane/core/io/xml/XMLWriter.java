/*
 * Modified by Dimitry Polivaev (2010)
 * 
 * XMLWriter.java NanoXML/Java $Revision: 1.4 $ $Date: 2002/03/24 11:37:51 $
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
package org.freeplane.core.io.xml;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Vector;

import org.freeplane.n3.nanoxml.XMLElement;

/**
 * An XMLWriter writes XML data to a stream.
 * 
 * @see org.freeplane.n3.nanoxml.XMLElement
 * @see java.io.Writer
 * @author Marc De Scheemaecker Modified by Dimitry Polivaev: optionally not
 *         write closing element tag
 */
class XMLWriter {
	/**
	 * Where to write the output to.
	 */
	private PrintWriter writer;
	private final boolean restrictedCharset;

	public void flush() {
	    writer.flush();
    }

	/**
	 * Creates a new XML writer.
	 * 
	 * @param writer
	 *            where to write the output to.
	 */
	public XMLWriter(final Writer writer, boolean restrictedCharset) {
		this.restrictedCharset = restrictedCharset;
		if (writer instanceof PrintWriter) {
			this.writer = (PrintWriter) writer;
		}
		else {
			this.writer = new PrintWriter(writer);
		}
	}

	public void endElement(final String fullName, final boolean prettyPrint) {
		writer.print("</" + fullName + '>');
		if (prettyPrint) {
			writer.println();
		}
	}

	/**
	 * Cleans up the object when it's destroyed.
	 */
	@Override
	protected void finalize() throws Throwable {
		flush();
		writer = null;
		super.finalize();
	}

	/**
	 * Writes an XML element.
	 * 
	 * @param xml
	 *            the non-null XML element to write.
	 */
	public void startElement(final XMLElement xml) throws IOException {
		this.write(xml, false, 0, true, false);
	}

	/**
	*/
	public void write(final String content) {
		writeEncoded(content, false, true);
	}

	/**
	 * Writes an XML element.
	 * 
	 * @param xml
	 *            the non-null XML element to write.
	 */
	public void write(final XMLElement xml) throws IOException {
		this.write(xml, false, 0, true, true);
	}

	/**
	 * Writes an XML element.
	 * 
	 * @param xml
	 *            the non-null XML element to write.
	 * @param prettyPrint
	 *            if spaces need to be inserted to make the output more readable
	 * @param indent
	 *            how many spaces to indent the element.
	 * @param endElement
	 */
	protected void write(final XMLElement xml, final boolean prettyPrint, final int indent,
	                     final boolean collapseEmptyElements, final boolean endElement) throws IOException {
		if (prettyPrint) {
			for (int i = 0; i < indent; i++) {
				writer.print(' ');
			}
		}
		if (xml.getName() == null) {
			if (xml.getContent() != null) {
				if (prettyPrint) {
					this.writeEncoded(xml.getContent().trim(), false, false);
					writer.println();
				}
				else {
					this.writeEncoded(xml.getContent(), false, false);
				}
			}
		}
		else {
			writer.print('<');
			final String fullName = xml.getFullName();
			writer.print(fullName);
			final Vector<String> nsprefixes = new Vector<String>();
			if (xml.getNamespace() != null) {
				if (xml.getName().equals(fullName)) {
					writer.print(" xmlns=\"" + xml.getNamespace() + '"');
				}
				else {
					String prefix = fullName;
					prefix = prefix.substring(0, prefix.indexOf(':'));
					nsprefixes.addElement(prefix);
					writer.print(" xmlns:" + prefix);
					writer.print("=\"" + xml.getNamespace() + "\"");
				}
			}
			Enumeration<String> enumAttributeNames = xml.enumerateAttributeNames();
			while (enumAttributeNames.hasMoreElements()) {
				final String key = (String) enumAttributeNames.nextElement();
				final int index = key.indexOf(':');
				if (index >= 0) {
					final String namespace = xml.getAttributeNamespace(key);
					if (namespace != null) {
						final String prefix = key.substring(0, index);
						if (!nsprefixes.contains(prefix)) {
							writer.print(" xmlns:" + prefix);
							writer.print("=\"" + namespace + '"');
							nsprefixes.addElement(prefix);
						}
					}
				}
			}
			enumAttributeNames = xml.enumerateAttributeNames();
			while (enumAttributeNames.hasMoreElements()) {
				final String key = (String) enumAttributeNames.nextElement();
				final String value = xml.getAttribute(key, null);
				writer.print(" " + key + "=\"");
				this.writeEncoded(value, true, false);
				writer.print('"');
			}
			if ((xml.getContent() != null) && (xml.getContent().length() > 0)) {
				writer.print('>');
				this.writeEncoded(xml.getContent(), false, false);
				if (endElement) {
					endElement(fullName, prettyPrint);
				}
			}
			else if (xml.hasChildren() || (!collapseEmptyElements)) {
				writer.print('>');
				if (prettyPrint) {
					writer.println();
				}
				Enumeration<XMLElement> enumeration = xml.enumerateChildren();
				while (enumeration.hasMoreElements()) {
					final XMLElement child = enumeration.nextElement();
					this.write(child, prettyPrint, indent + 4, collapseEmptyElements, true);
				}
				if (prettyPrint) {
					for (int i = 0; i < indent; i++) {
						writer.print(' ');
					}
				}
				if (endElement) {
					endElement(fullName, prettyPrint);
				}
			}
			else {
				if (endElement) {
					writer.print("/>");
				}
				else {
					writer.print(">");
				}
				if (prettyPrint) {
					writer.println();
				}
			}
		}
	}

	/**
	 * Writes a string encoding reserved characters.
	 */
	private void writeEncoded(final String str, final boolean atributeValue, final boolean xmlInclude) {
		for (int i = 0; i < str.length(); i++) {
			final char c = str.charAt(i);
			if (c > 0x7E) {
				if (restrictedCharset) {
					writer.print("&#x");
					writer.print(Integer.toString(c, 16));
					writer.print(';');
				}
				else {
					writer.print(c);
				}
				continue;
			}
			else if (xmlInclude) {
				writer.print(c);
				continue;
			}
			switch (c) {
				case '<':
					writer.print("&lt;");
					continue;
				case '>':
					writer.print("&gt;");
					continue;
				case '&':
					writer.print("&amp;");
					continue;
				case '\'':
					writer.print("&apos;");
					continue;
				case '"':
					writer.print("&quot;");
					continue;
				case 0x0A:
					if (atributeValue) {
						writer.print("&#xa;");
					}
					else {
						writer.print(c);
					}
					continue;
				default:
					if (c < ' ') {
						writer.print("&#x");
						writer.print(Integer.toString(c, 16));
						writer.print(';');
						continue;
					}
					writer.print(c);
			}
		}
	}
}
