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

import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeplaneAction;
import org.freeplane.controller.resources.ResourceController;
import org.freeplane.modes.ModeController;

class ShowHideNoteAction extends FreeplaneAction {
	/**
	 *
	 */
	final private MNoteController noteController;

	public ShowHideNoteAction(final MNoteController noteController,
	                          final ModeController modeController) {
		super("accessories/plugins/NodeNote_hide_show.properties_name");
		this.noteController = noteController;
	}

	public void actionPerformed(final ActionEvent e) {
		if (noteController.getSplitPane() == null) {
			noteController.getSplitPaneToScreen();
		}
		else {
			(noteController).hideNotesPanel();
			Controller.getResourceController().setProperty(
			    ResourceController.RESOURCES_USE_SPLIT_PANE, "false");
		}
	}
}
