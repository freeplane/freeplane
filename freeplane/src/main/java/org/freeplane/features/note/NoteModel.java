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

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.RichTextModel;

/**
 * @author Dimitry Polivaev
 */
public class NoteModel extends RichTextModel implements IExtension {
	public static final String EDITING_PURPOSE = "Note";

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
		return extension != null ? extension.getText() : null;
	}

}
