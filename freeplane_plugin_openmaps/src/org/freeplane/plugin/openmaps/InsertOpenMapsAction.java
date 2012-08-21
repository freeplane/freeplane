package org.freeplane.plugin.openmaps;


import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

public class InsertOpenMapsAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private static final String actionIdentifier = "OpenMapsAddInformation";
	private final OpenMapsNodeHook mapNodeHook;

	public InsertOpenMapsAction(OpenMapsNodeHook nodeHook) {
		super(actionIdentifier);
		this.mapNodeHook = nodeHook;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		mapNodeHook.chooseLocation();
	}

}
