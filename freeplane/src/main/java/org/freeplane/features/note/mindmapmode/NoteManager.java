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
package org.freeplane.features.note.mindmapmode;

import java.net.URL;
import java.util.regex.Pattern;

import javax.swing.text.html.HTMLDocument;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.note.mindmapmode.MNoteController.NoteDocumentListener;

import com.lightdev.app.shtm.SHTMLPanel;

final class NoteManager implements INodeSelectionListener, IMapSelectionListener {
	public final static Pattern HEAD = Pattern.compile("<head>.*</head>\n", Pattern.DOTALL);
	private boolean ignoreEditorUpdate;
	NoteDocumentListener mNoteDocumentListener;
	private NodeModel node;
	/**
	 *
	 */
	final private MNoteController noteController;

	public NoteManager(final MNoteController noteController) {
		this.noteController = noteController;
	}

	public void onDeselect(final NodeModel node) {
		final SHTMLPanel noteViewerComponent = noteController.getNoteViewerComponent();
		if (noteViewerComponent == null) {
			return;
		}
		noteViewerComponent.getDocument().removeDocumentListener(mNoteDocumentListener);
		saveNote(node);
		this.node = null;
	}

	public void onSelect(final NodeModel nodeView) {
		node = nodeView;
		updateEditor();
	}

	void saveNote() {
		if (node == null) {
			return;
		}
		final SHTMLPanel noteViewerComponent = noteController.getNoteViewerComponent();
		if (noteViewerComponent == null) {
			return;
		}
		boolean editorContentEmpty = true;
		String documentText = noteViewerComponent.getDocumentText();
		documentText = HEAD.matcher(documentText).replaceFirst("");
		editorContentEmpty = HtmlUtils.isEmpty(documentText);
		Controller.getCurrentModeController().getMapController().removeNodeSelectionListener(this);
		if (noteViewerComponent.needsSaving()) {
			try {
				ignoreEditorUpdate = true;
				if (editorContentEmpty) {
					noteController.setNoteText(node, null);
				}
				else {
					noteController.setNoteText(node, documentText);
				}
			}
			finally {
				ignoreEditorUpdate = false;
			}
		}
		Controller.getCurrentModeController().getMapController().addNodeSelectionListener(this);
	}

	void saveNote(final NodeModel node) {
		if (this.node != node) {
			return;
		}
		saveNote();
	}

	void updateEditor() {
		if (ignoreEditorUpdate) {
			return;
		}
		final SHTMLPanel noteViewerComponent = noteController.getNoteViewerComponent();
		if (noteViewerComponent == null) {
			return;
		}
		final HTMLDocument document = noteViewerComponent.getDocument();
		document.removeDocumentListener(mNoteDocumentListener);
		try {
			final URL url = node.getMap().getURL();
			if (url != null) {
				document.setBase(url);
			}
			else {
				document.setBase(new URL("file: "));
			}
		}
		catch (final Exception e) {
		}
		noteController.setDefaultStyle(node);
		final String note = node != null ? NoteModel.getNoteText(node) : null;
		if (note != null)
			noteViewerComponent.setCurrentDocumentContent(note);
		else 
			noteViewerComponent.setCurrentDocumentContent("");
		document.addDocumentListener(mNoteDocumentListener);
	}

	@Override
	public void afterMapChange(MapModel oldMap, MapModel newMap) {
		if(newMap == null) {
			node = null;
			final SHTMLPanel noteViewerComponent = noteController.getNoteViewerComponent();
			if(noteViewerComponent != null)
				noteViewerComponent.setCurrentDocumentContent("");
		}
	}

	@Override
	public void beforeMapChange(MapModel oldMap, MapModel newMap) {
	}

	NodeModel getNode() {
		return node;
	}
}
