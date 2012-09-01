package org.freeplane.plugin.openmaps.actions;

import java.awt.event.ActionEvent;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.plugin.openmaps.OpenMapsNodeHook;


public class InsertOpenMapsAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private static final String actionIdentifier = "OpenMapsAddInformation";
	
	public InsertOpenMapsAction() {
		super(actionIdentifier);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		final OpenMapsNodeHook nodeHook = new OpenMapsNodeHook();
		nodeHook.chooseLocation();
	}



}
