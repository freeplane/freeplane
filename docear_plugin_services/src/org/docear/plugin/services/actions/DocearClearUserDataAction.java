package org.docear.plugin.services.actions;

import java.awt.event.ActionEvent;

import org.docear.plugin.services.communications.CommunicationsController;
import org.freeplane.core.ui.AFreeplaneAction;

public class DocearClearUserDataAction extends AFreeplaneAction {

	
	private static final long serialVersionUID = 1L;
	public static final String KEY = "docear.clear.user_data";
	
	public DocearClearUserDataAction() {
		super(KEY);
	}
	
	public void actionPerformed(ActionEvent e) {
		CommunicationsController.getController().resetRegisteredUser();
	}

}
