package org.freeplane.view.swing.map;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.note.NoteController;
import org.freeplane.features.mindmapmode.note.MNoteController;

@SuppressWarnings("serial")
@SelectableAction(checkOnPopup=true)
public class ShowNotesInMapAction extends AFreeplaneAction {

	public ShowNotesInMapAction() {
		super("ShowNotesInMapAction");
	}

	public void actionPerformed(ActionEvent e) {
		final MapModel map = Controller.getCurrentController().getMap();
		final MNoteController noteController = (MNoteController) NoteController.getController();
		noteController.setShowNotesInMap(map, ! NoteController.getController().showNotesInMap(map));
		setSelected();
	}

	@Override
	public void setSelected() {
		final MapModel map = Controller.getCurrentController().getMap();
		final boolean notesShown = map != null && NoteController.getController().showNotesInMap(map);
		setSelected(notesShown);
	}


}
