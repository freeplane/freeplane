package org.freeplane.view.swing.map;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.mindmapmode.MNoteController;

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
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		selection.preserveSelectedNodeLocationOnScreen();
		setSelected();
	}

	@Override
	public void setSelected() {
		final MapModel map = Controller.getCurrentController().getMap();
		final boolean notesShown = map != null && NoteController.getController().showNotesInMap(map);
		setSelected(notesShown);
	}


}
