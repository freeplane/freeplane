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
package org.freeplane.map.note;

import java.io.IOException;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.XMLElement;
import org.freeplane.core.map.NodeModel;
import org.freeplane.map.text.NodeTextBuilder;

/**
 * @author Dimitry Polivaev
 */
public class NoteWriter implements IExtensionElementWriter {
	NoteController noteManager;

	public NoteWriter(final NoteController noteManager) {
		super();
		this.noteManager = noteManager;
	}

	/**
	 * @param writer
	 * @param note
	 * @throws IOException
	 */
	private void saveContent(final ITreeWriter writer, final NoteModel note) throws IOException {
		if (note.getXmlNoteText() != null) {
			final XMLElement htmlElement = new XMLElement();
			htmlElement.setName(NodeTextBuilder.XML_NODE_XHTML_CONTENT_TAG);
			htmlElement.setAttribute(NodeTextBuilder.XML_NODE_XHTML_TYPE_TAG,
			    NodeTextBuilder.XML_NODE_XHTML_TYPE_NOTE);
			final String content = note.getXmlNoteText().replace('\0', ' ');
			writer.addElement(content, htmlElement);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.io.INodeWriter#saveContent(freeplane.io.ITreeWriter,
	 * java.lang.Object, java.lang.String)
	 */
	public void writeContent(final ITreeWriter writer, final Object element, final IExtension note)
	        throws IOException {
		final NodeModel node = (NodeModel) element;
		noteManager.onWrite(node);
		saveContent(writer, (NoteModel) note);
		return;
	}
}
