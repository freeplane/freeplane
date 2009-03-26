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
package org.freeplane.features.common.note;

import org.freeplane.core.io.IElementContentHandler;
import org.freeplane.core.model.NodeModel;
import org.freeplane.features.common.text.NodeTextBuilder;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
class NoteBuilder implements IElementContentHandler {
	final private NoteController noteController;

	public NoteBuilder(final NoteController noteController) {
		super();
		this.noteController = noteController;
	}

	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (attributes == null) {
			return null;
		}
		final Object typeAttribute = attributes.getAttribute(NodeTextBuilder.XML_NODE_XHTML_TYPE_TAG, null);
		if (typeAttribute == null || NodeTextBuilder.XML_NODE_XHTML_TYPE_NODE.equals(typeAttribute)) {
			return null;
		}
		return parent;
	}

	public void endElement(final Object parent, final String tag, final Object node, final XMLElement attributes,
	                       final String content) {
		if (tag.equals("richcontent")) {
			final String xmlText = content;
			final NoteModel note = new NoteModel();
			note.setXmlNoteText(xmlText);
			((NodeModel) node).addExtension(note);
			noteController.setStateIcon(((NodeModel) node), true);
		}
	}
}
