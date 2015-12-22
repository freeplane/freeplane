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
package org.freeplane.features.note;

import java.io.IOException;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeWriter;
import org.freeplane.features.text.NodeTextBuilder;
import org.freeplane.features.text.RichTextModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
class NoteWriter implements IExtensionElementWriter, IAttributeWriter {
	NoteController noteManager;

	public NoteWriter(final NoteController noteManager) {
		super();
		this.noteManager = noteManager;
	}

	public void writeAttributes(final ITreeWriter writer, final Object userObject, final String tag) {
		if(! NodeWriter.shouldWriteSharedContent(writer))
			return;
		noteManager.onWrite((MapModel) userObject);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.io.INodeWriter#saveContent(freeplane.io.ITreeWriter,
	 * java.lang.Object, java.lang.String)
	 */
	public void writeContent(final ITreeWriter writer, final Object element, final IExtension note) throws IOException {
		RichTextModel note1 = (RichTextModel) note;
		if (note1.getXml() != null) {
        	final XMLElement htmlElement = new XMLElement();
    		htmlElement.setName(NodeTextBuilder.XML_NODE_XHTML_CONTENT_TAG);
        	if(note instanceof NoteModel){
            	htmlElement.setAttribute(NodeTextBuilder.XML_NODE_XHTML_TYPE_TAG, NodeTextBuilder.XML_NODE_XHTML_TYPE_NOTE);
        	}
        	else{
        		htmlElement.setAttribute(NodeTextBuilder.XML_NODE_XHTML_TYPE_TAG, "UNKNOWN");
        	}
        	final String content = note1.getXml().replace('\0', ' ');
        	writer.addElement('\n' + content + '\n', htmlElement);
        }
		return;
	}
}
