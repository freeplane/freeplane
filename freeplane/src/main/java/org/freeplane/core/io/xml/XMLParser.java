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
	static enum ContentCollect{NONE, BY_CLOSING_TAG_LEVEL, BY_CLOSING_TAG_NAME}
	ContentCollect contentCollect = ContentCollect.NONE;


	public XMLParser() {
		super();
		setResolver(new LocalEntityResolver());
	}

	void collectContentUntilMatchingTag(boolean findsClosingTagByName) {
	    contentCollect = findsClosingTagByName ? ContentCollect.BY_CLOSING_TAG_NAME : ContentCollect.BY_CLOSING_TAG_LEVEL;
	}

	@Override
	protected void processElement(final String defaultNamespace, final Properties namespaces) throws Exception {
		try {
			super.processElement(defaultNamespace, namespaces);
		}
		finally {
		    contentCollect = ContentCollect.NONE;
		}
	}

	@Override
	protected void processElementContent(final String defaultNamespace, final Properties namespaces,
	                                     final String fullName, final String name, final String prefix)
	        throws IOException, XMLParseException, Exception {
		if (contentCollect != ContentCollect.NONE) {
			boolean inComment = false;
			final TreeXmlReader builder = (TreeXmlReader) getBuilder();
			final StringBuilder contentBuffer = new StringBuilder();
			int level = 1;
			int confirmedContentBufferLength = 0;
            int closingTagMatchingCharacters = -2;
			for (;;) {
				final IXMLReader reader = getReader();
				char ch = reader.read();
				if (inComment) {
					contentBuffer.append(ch);
					if (ch != '-') {
						continue;
					}
					ch = reader.read();
					contentBuffer.append(ch);
					if (ch != '-') {
						continue;
					}
					ch = reader.read();
					contentBuffer.append(ch);
					if (ch != '>') {
						continue;
					}
					inComment = false;
					continue;
				}
				if (ch == '<') {
					ch = reader.read();
					if (ch == '/') {
					    if (contentCollect == ContentCollect.BY_CLOSING_TAG_LEVEL) {
					        level--;
					        if (level == 0) {
					            break;
					        }
					    }
					    else {
					        confirmedContentBufferLength = contentBuffer.length();
					        closingTagMatchingCharacters = -1;

					    }
					}
					else if (contentCollect == ContentCollect.BY_CLOSING_TAG_LEVEL) {
					    if (ch == '!') {
					        final char read1 = reader.read();
					        final char read2 = reader.read();
					        if (read1 != '-' || read2 != '-') {
					            throw new XMLParseException(reader.getSystemID(), reader.getLineNr(), "Invalid input: <!"
					                    + read1 + read2);
					        }
					        inComment = true;
					        contentBuffer.append("<!--");
					        continue;
					    }
					    else {
					        level++;
					    }
					}
					contentBuffer.append('<');
				}
				else if (contentCollect == ContentCollect.BY_CLOSING_TAG_LEVEL && ch == '/') {
				    ch = reader.read();
				    if (ch == '>') {
				        level--;
				        if (level == 0) {
				            throw new XMLParseException(reader.getSystemID(), reader.getLineNr(), "Invalid input: />");
				        }
				    }
				    else if (ch == '<') {
				        contentBuffer.append('/');
				        reader.unread(ch);
				        continue;
				    }
				    contentBuffer.append('/');
				}
				if(contentCollect == ContentCollect.BY_CLOSING_TAG_NAME) {
				    if (closingTagMatchingCharacters == -1
				            || closingTagMatchingCharacters >= 0 && closingTagMatchingCharacters < fullName.length() && fullName.charAt(closingTagMatchingCharacters) == ch) {
				        if ((closingTagMatchingCharacters >= 0 && closingTagMatchingCharacters < fullName.length())
				                || (ch != ' ' && ch != '\t' && ch != '\n'))
				            closingTagMatchingCharacters++;
				    } else if(ch == '>' && closingTagMatchingCharacters == fullName.length()) {
				        contentBuffer.setLength(confirmedContentBufferLength);
				        closingTagIsReadAndVerified = true;
				        break;
				    }
				    else
				        closingTagMatchingCharacters = -2;
				}
				contentBuffer.append(ch);
			}
			builder.setElementContent(contentBuffer.toString());
			return;
		}
		super.processElementContent(defaultNamespace, namespaces, fullName, name, prefix);
	}
}
