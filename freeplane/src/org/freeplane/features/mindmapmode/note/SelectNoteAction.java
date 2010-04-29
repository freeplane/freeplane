/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.mindmapmode.note;

import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;

import javax.swing.JEditorPane;
import javax.swing.JSplitPane;

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;

import com.lightdev.app.shtm.SHTMLPanel;

class SelectNoteAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	final private MNoteController noteController;

	public SelectNoteAction(final MNoteController noteController, final ModeController modeController) {
		super("SelectNoteAction", modeController.getController());
		this.noteController = noteController;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
	 * )
	 */
	public void actionPerformed(final ActionEvent e) {
		if (noteController.isEditing()) {
			noteController.setFocusToMap();
			return;
		}
		final JSplitPane splitPane = noteController.getSplitPaneToScreen();
		final int oldSize = splitPane.getDividerLocation();
		noteController.setPositionToRecover(new Integer(oldSize));
		KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				final SHTMLPanel htmlEditorPanel = noteController.getHtmlEditorPanel();
				htmlEditorPanel.getMostRecentFocusOwner().requestFocus();
				if (ResourceController.getResourceController().getBooleanProperty("goto_note_end_on_edit")) {
					final JEditorPane editorPane = htmlEditorPanel.getEditorPane();
					editorPane.setCaretPosition(editorPane.getDocument().getLength());
				}
			}
		});
	}
}
