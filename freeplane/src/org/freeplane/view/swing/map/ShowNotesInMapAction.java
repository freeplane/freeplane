package org.freeplane.view.swing.map;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;

@SuppressWarnings("serial")
@SelectableAction(checkOnPopup=true)
public class ShowNotesInMapAction extends AFreeplaneAction {

	public ShowNotesInMapAction() {
		super("ShowNotesInMapAction");
	}

	public void actionPerformed(ActionEvent e) {
		MapView map = (MapView)Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		map.setShowNotes(!map.showNotes());
		setSelected();
	}

	@Override
	public void setSelected() {
		MapView map = (MapView)Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		setSelected(map.showNotes());
	}


}
