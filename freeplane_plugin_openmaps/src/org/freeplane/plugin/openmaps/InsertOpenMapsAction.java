package org.freeplane.plugin.openmaps;


import java.awt.Point;
import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

public class InsertOpenMapsAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private static final String actionIdentifier = "OpenMapsAddInformation";
	
	public InsertOpenMapsAction() {
		super(actionIdentifier);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		final MapViewer map = new MapViewer();
		Point locationChoosen = null;
		while (locationChoosen == null) {
			locationChoosen = map.getController().getSelectedLocation();
		}
		System.out.println("Location choosen" + locationChoosen);
	}

}
