/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.view.swing.features.time.mindmapmode.nodelist;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.note.mindmapmode.MNoteController;

/**
 * @author  Dimitry Polivaev 03.10.2013
 */
class NoteTextAccessor implements TextAccessor {
	final private NodeModel node;

	public NoteTextAccessor(NodeModel node) {
		this.node = node;
	}

	public String getText() {
	    final String notesText = NoteModel.getNoteText(node);
	    return notesText != null ? notesText : "";
	}

	public void setText(String newText) {
		((MNoteController) Controller.getCurrentModeController().getExtension(NoteController.class)).setNoteText(node, newText);
    }

	public NodeModel getNode() {
	    return node;
    }
}