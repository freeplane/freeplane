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
package org.freeplane.map.note.mindmapnode;

import javax.swing.text.html.HTMLDocument;

import org.freeplane.core.map.NodeModel;
import org.freeplane.core.mode.INodeSelectionListener;
import org.freeplane.map.note.mindmapnode.MNoteController.NoteDocumentListener;
import org.freeplane.view.map.NodeView;

final class NoteManager implements INodeSelectionListener {
	public final static String EMPTY_EDITOR_STRING = "<html>\n  <head>\n\n  </head>\n  <body>\n    <p>\n      \n    </p>\n  </body>\n</html>\n";
	public final static String EMPTY_EDITOR_STRING_ALTERNATIVE = "<html>\n  <head>\n    \n  </head>\n  <body>\n    <p>\n      \n    </p>\n  </body>\n</html>\n";
	NoteDocumentListener mNoteDocumentListener;
	private NodeModel node;
	/**
	 *
	 */
	final private MNoteController noteController;

	public NoteManager(final MNoteController noteController) {
		this.noteController = noteController;
	}

	public void onDeselect(final NodeView node) {
		noteController.getNoteViewerComponent().getDocument().removeDocumentListener(
		    mNoteDocumentListener);
		onWrite(node.getModel());
		this.node = null;
	}

	public void onSelect(final NodeView nodeView) {
		node = nodeView.getModel();
		updateEditor();
	}

	void onWrite(final NodeModel node) {
		if (this.node != node) {
			return;
		}
		boolean editorContentEmpty = true;
		final String documentText = noteController.getNoteViewerComponent().getDocumentText();
		editorContentEmpty = documentText.equals(EMPTY_EDITOR_STRING)
		        || documentText.equals(EMPTY_EDITOR_STRING_ALTERNATIVE);
		noteController.getModeController().removeNodeSelectionListener(this);
		if (noteController.getNoteViewerComponent().needsSaving()) {
			if (editorContentEmpty) {
				noteController.setNoteText(node, null);
			}
			else {
				noteController.setNoteText(node, documentText);
			}
			noteController.setLastContentEmpty(editorContentEmpty);
		}
		noteController.getModeController().addNodeSelectionListener(this);
	}

	void updateEditor() {
		final HTMLDocument document = noteController.getNoteViewerComponent().getDocument();
		document.removeDocumentListener(mNoteDocumentListener);
		try {
			document.setBase(node.getMap().getFile().toURL());
		}
		catch (final Exception e) {
		}
		final String note = node.getNoteText();
		if (note != null) {
			noteController.getNoteViewerComponent().setCurrentDocumentContent(note);
			noteController.setLastContentEmpty(false);
		}
		else if (!noteController.isLastContentEmpty()) {
			noteController.getNoteViewerComponent().setCurrentDocumentContent("");
			noteController.setLastContentEmpty(true);
		}
		document.addDocumentListener(mNoteDocumentListener);
	}
}
