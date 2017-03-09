/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.core.io.xml;

import java.io.IOException;
import java.util.Properties;

import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLParser;
import org.freeplane.n3.nanoxml.XMLParseException;

class XMLParser extends StdXMLParser implements IXMLParser {
	private boolean skipNextElementContent = false;

	public XMLParser() {
		super();
		setResolver(new LocalEntityResolver());
	}

	void notParseNextElementContent() {
		skipNextElementContent = true;
	}

	@Override
	protected void processElement(final String defaultNamespace, final Properties namespaces) throws Exception {
		try {
			super.processElement(defaultNamespace, namespaces);
		}
		finally {
			skipNextElementContent = false;
		}
	}

	@Override
	protected void processElementContent(final String defaultNamespace, final Properties namespaces,
	                                     final String fullName, final String name, final String prefix)
	        throws IOException, XMLParseException, Exception {
		if (skipNextElementContent) {
			boolean inComment = false;
			final TreeXmlReader builder = (TreeXmlReader) getBuilder();
			final StringBuilder waitingBuf = new StringBuilder();
			int level = 1;
			for (;;) {
				final IXMLReader reader = getReader();
				char ch = reader.read();
				if (inComment) {
					waitingBuf.append(ch);
					if (ch != '-') {
						continue;
					}
					ch = reader.read();
					waitingBuf.append(ch);
					if (ch != '-') {
						continue;
					}
					ch = reader.read();
					waitingBuf.append(ch);
					if (ch != '>') {
						continue;
					}
					inComment = false;
					continue;
				}
				if (ch == '<') {
					ch = reader.read();
					if (ch == '/') {
						level--;
						if (level == 0) {
							break;
						}
					}
					else if (ch == '!') {
						final char read1 = reader.read();
						final char read2 = reader.read();
						if (read1 != '-' || read2 != '-') {
							throw new XMLParseException(reader.getSystemID(), reader.getLineNr(), "Invalid input: <!"
							        + read1 + read2);
						}
						inComment = true;
						waitingBuf.append("<!--");
						continue;
					}
					else {
						level++;
					}
					waitingBuf.append('<');
				}
				else if (ch == '/') {
					ch = reader.read();
					if (ch == '>') {
						level--;
						if (level == 0) {
							throw new XMLParseException(reader.getSystemID(), reader.getLineNr(), "Invalid input: />");
						}
					}
					else if (ch == '<') {
						waitingBuf.append('/');
						reader.unread(ch);
						continue;
					}
					waitingBuf.append('/');
				}
				waitingBuf.append(ch);
			}
			builder.setElementContent(waitingBuf.toString());
			return;
		}
		super.processElementContent(defaultNamespace, namespaces, fullName, name, prefix);
	}
}
