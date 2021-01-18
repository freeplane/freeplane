/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.features.note;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IElementContentHandler;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.ContentSyntax;
import org.freeplane.features.text.NodeTextBuilder;
import org.freeplane.features.text.RichTextModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
class NoteBuilder implements IElementContentHandler {
	public NoteBuilder() {
		super();
	}

	public Object createElement(final Object parent, final String tag, final XMLElement element) {
		if (element == null) {
			return null;
		}
		final Object typeAttribute = element.getAttribute(NodeTextBuilder.XML_RICHCONTENT_TYPE_ATTRIBUTE, null);
		if (! NodeTextBuilder.XML_RICHCONTENT_TYPE_NOTE.equals(typeAttribute)) {
			return null;
		}
		return parent;
	}

	public void endElement(final Object parent, final String tag, final Object node, final XMLElement element,
	        final String content) {
	    if (tag.equals("richcontent")) {
	        final String text;
	        if(content != null)
	            text = content.trim();
	        else
	            text = null;
	        final Object typeAttribute = element.getAttribute(NodeTextBuilder.XML_RICHCONTENT_TYPE_ATTRIBUTE, null);
			if (NodeTextBuilder.XML_RICHCONTENT_TYPE_NOTE.equals(typeAttribute)) {
				final NoteModel note = new NoteModel();
	            if(containsXml(element))
	                note.setXml(text);
	            else
	                note.setText(text);
	            final String contentType = element.getAttribute(
	                    NodeTextBuilder.XML_RICHCONTENT_CONTENT_TYPE_ATTRIBUTE, 
	                    ContentSyntax.XML.prefix);
	            note.setContentType(ContentSyntax.specificType(contentType));

				((NodeModel) node).addExtension((IExtension) note);
			}
		}
	}

    @Override
    public boolean containsXml(XMLElement element) {
        return ContentSyntax.XML.matches(element.getAttribute(NodeTextBuilder.XML_RICHCONTENT_CONTENT_TYPE_ATTRIBUTE, ContentSyntax.XML.prefix));
    }}
