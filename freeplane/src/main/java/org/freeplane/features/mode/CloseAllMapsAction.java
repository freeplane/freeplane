package org.freeplane.features.mode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

@SuppressWarnings("serial")
class CloseAllMapsAction extends AFreeplaneAction{

	public CloseAllMapsAction() {
		super("CloseAllMapsAction");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Controller.getCurrentController().closeAllMaps();
	}

}
