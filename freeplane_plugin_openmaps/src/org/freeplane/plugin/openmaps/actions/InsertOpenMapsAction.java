package org.freeplane.plugin.openmaps.actions;

import java.awt.event.ActionEvent;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.plugin.openmaps.mapElements.MapViewer;

public class InsertOpenMapsAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private static final String actionIdentifier = "OpenMapsAddInformation";
	
	public InsertOpenMapsAction() {
		super(actionIdentifier);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		final MapViewer map = new MapViewer();
		/* Point locationChoosen = null;
		//While loop needs to be replaced with a listener
		while (locationChoosen == null) {
			locationChoosen = map.getController().getSelectedLocation();
		}
		//addChoosenLocationToSelectedNode(locationChoosen); */
	}



}
