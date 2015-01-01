package org.freeplane.view.swing.map;

import java.awt.event.MouseEvent;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.features.text.TextController;
import org.freeplane.view.swing.ui.LinkNavigatorMouseListener;
import org.freeplane.view.swing.ui.NodeSelector;

public class NoteViewMouseListener extends LinkNavigatorMouseListener {

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2) {
			TextController controller = TextController.getController();
			if(controller.canEdit()){
				NodeModel node = new NodeSelector().getRelatedNodeView(e).getModel();
				((MNoteController) NoteController.getController()).editNoteInDialog(node);
			}
		}
		else
			super.mouseClicked(e);
	}

}
