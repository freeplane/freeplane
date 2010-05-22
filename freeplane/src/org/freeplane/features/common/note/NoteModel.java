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
package org.freeplane.features.common.note;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.XmlUtils;
import org.freeplane.features.common.map.NodeModel;

/**
 * @author Dimitry Polivaev
 */
public class NoteModel implements IExtension {
	public static NoteModel createNote(final NodeModel node) {
		NoteModel note = NoteModel.getNote(node);
		if (note == null) {
			note = new NoteModel();
			node.addExtension(note);
		}
		return note;
	}

	public static NoteModel getNote(final NodeModel node) {
		final NoteModel extension = (NoteModel) node.getExtension(NoteModel.class);
		return extension;
	}

	public static String getNoteText(final NodeModel node) {
		final NoteModel extension = NoteModel.getNote(node);
		return extension != null ? extension.getNoteText() : null;
	}

	public static String getXmlNoteText(final NodeModel node) {
		final NoteModel extension = NoteModel.getNote(node);
		return extension != null ? extension.getXmlNoteText() : null;
	}

	private String noteText = null;
	private String xmlNoteText = null;

	public String getNoteText() {
		return noteText;
	}

	public String getXmlNoteText() {
		return xmlNoteText;
	}

	public final void setNoteText(final String pNoteText) {
		if (pNoteText == null) {
			xmlNoteText = null;
			noteText = null;
			return;
		}
		noteText = XmlUtils.makeValidXml(pNoteText);
		xmlNoteText = HtmlUtils.toXhtml(noteText);
		if (xmlNoteText != null && !xmlNoteText.startsWith("<")) {
			noteText = xmlNoteText;
		}
	}

	public final void setXmlNoteText(final String pXmlNoteText) {
		if (pXmlNoteText == null) {
			xmlNoteText = null;
			noteText = null;
			return;
		}
		xmlNoteText = XmlUtils.makeValidXml(pXmlNoteText);
		noteText = HtmlUtils.getInstance().toHtml(xmlNoteText);
	}
}
