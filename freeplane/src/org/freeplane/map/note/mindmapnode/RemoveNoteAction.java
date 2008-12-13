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
package org.freeplane.map.note.mindmapnode;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeplaneAction;
import org.freeplane.controller.resources.ResourceController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.common.OptionalDontShowMeAgainDialog;

class RemoveNoteAction extends FreeplaneAction implements PopupMenuListener {
	/**
	 *
	 */
	final private MNoteController noteController;

	public RemoveNoteAction(final MNoteController noteController,
	                        final ModeController modeController) {
		super("accessories/plugins/RemoveNote.properties_name");
		this.noteController = noteController;
	}

	public void actionPerformed(final ActionEvent e) {
		final int showResult = new OptionalDontShowMeAgainDialog(Controller
		    .getController().getViewController().getJFrame(),
		    ((MModeController) getModeController()).getSelectedView(),
		    "really_remove_notes", "confirmation",
		    new OptionalDontShowMeAgainDialog.StandardPropertyHandler(
		        ResourceController.RESOURCES_REMOVE_NOTES_WITHOUT_QUESTION),
		    OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED).show()
		    .getResult();
		if (showResult != JOptionPane.OK_OPTION) {
			return;
		}
		for (final Iterator iterator = ((MModeController) getModeController())
		    .getSelectedNodes().iterator(); iterator.hasNext();) {
			final NodeModel node = (NodeModel) iterator.next();
			if (node.getNoteText() != null) {
				removeNote(node);
			}
		}
	}

	@Override
	public boolean isEnabled() {
		boolean foundNote = false;
		final ModeController modeController = getModeController();
		if (modeController == null) {
			return false;
		}
		for (final Iterator iterator = modeController.getSelectedNodes()
		    .iterator(); iterator.hasNext();) {
			final NodeModel node = (NodeModel) iterator.next();
			if (node.getNoteText() != null) {
				foundNote = true;
				break;
			}
		}
		return foundNote;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.PopupMenuListener#popupMenuCanceled(javax.swing.event
	 * .PopupMenuEvent)
	 */
	public void popupMenuCanceled(final PopupMenuEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.PopupMenuListener#popupMenuWillBecomeInvisible(javax
	 * .swing.event.PopupMenuEvent)
	 */
	public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.PopupMenuListener#popupMenuWillBecomeVisible(javax.
	 * swing.event.PopupMenuEvent)
	 */
	public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
		setEnabled(isEnabled());
	}

	private void removeNote(final NodeModel node) {
		if (((MModeController) getModeController()).getSelectedNode() == node) {
			noteController.getHtmlEditorPanel().setCurrentDocumentContent("");
		}
		noteController.setNoteText(node, null);
	}
}
