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

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.core.util.Tools;

/**
 * @author Dimitry Polivaev
 */
public class NoteModel implements IExtension {
	public static String getNoteText(final NodeModel node) {
		final NoteModel extension = (NoteModel) node.getExtension(NoteModel.class);
		return extension != null ? extension.getNoteText() : null;
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
		noteText = Tools.makeValidXml(pNoteText);
		xmlNoteText = HtmlTools.getInstance().toXhtml(noteText);
	}

	public final void setXmlNoteText(final String pXmlNoteText) {
		if (pXmlNoteText == null) {
			xmlNoteText = null;
			noteText = null;
			return;
		}
		xmlNoteText = Tools.makeValidXml(pXmlNoteText);
		noteText = HtmlTools.getInstance().toHtml(xmlNoteText);
	}
}
