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
package org.freeplane.map.note;

import org.freeplane.io.INodeContentHandler;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.map.text.NodeTextBuilder;
import org.freeplane.map.tree.NodeBuilder.NodeObject;

/**
 * @author Dimitry Polivaev
 */
public class NoteBuilder implements INodeContentHandler {
	final private NoteController noteController;

	public NoteBuilder(final NoteController noteController) {
		super();
		this.noteController = noteController;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.io.INodeContentHandler#setContent(java.lang.Object,
	 * java.lang.String, freeplane.io.xml.n3.nanoxml.IXMLElement,
	 * java.lang.String)
	 */
	public boolean setContent(final Object node, final String tag, final IXMLElement attributes,
	                          final String content) {
		if (tag.equals("richcontent")) {
			final String xmlText = content;
			final Object typeAttribute = attributes.getAttribute(
			    NodeTextBuilder.XML_NODE_XHTML_TYPE_TAG, null);
			if (typeAttribute != null
			        && !NodeTextBuilder.XML_NODE_XHTML_TYPE_NODE.equals(typeAttribute)) {
				final NoteModel note = new NoteModel();
				note.setXmlNoteText(xmlText);
				((NodeObject) node).node.addExtension(note);
				noteController.setStateIcon(((NodeObject) node).node, true);
				return true;
			}
		}
		return false;
	}
}
