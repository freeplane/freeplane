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

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;

@SelectableAction(checkOnPropertyChange = "use_split_pane")
class ShowHideNoteAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	final private MNoteController noteController;

	public ShowHideNoteAction(final MNoteController noteController, final ModeController modeController) {
		super("ShowHideNoteAction", modeController.getController());
		this.noteController = noteController;
		setSelected(ResourceController.getResourceController().getBooleanProperty(MNoteController.RESOURCES_USE_SPLIT_PANE));
	}

	public void actionPerformed(final ActionEvent e) {
		if (noteController.getSplitPane() == null) {
			noteController.getSplitPaneToScreen();
		}
		else {
			(noteController).hideNotesPanel();
			final Controller controller = getModeController().getController();
			final NodeModel node = controller.getSelection().getSelected();
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					Component component = controller.getViewController().getComponent(node);
					if(component != null){
						component.requestFocus();
					}
				}
			});
			ResourceController.getResourceController().setProperty(MNoteController.RESOURCES_USE_SPLIT_PANE, "false");
		}
	}

	@Override
	public void setSelected() {
		setSelected(noteController.getSplitPane() != null);
	}
}
