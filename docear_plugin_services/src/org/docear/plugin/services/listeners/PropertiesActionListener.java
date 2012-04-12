package org.docear.plugin.services.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.docear.plugin.services.actions.DocearAllowUploadChooserAction;

public class PropertiesActionListener implements ActionListener{

	public void actionPerformed(ActionEvent e) {
		if ("docear.allow.upload.action".equals(e.getActionCommand())) {
			DocearAllowUploadChooserAction.showDialog(true);
		}
		
	}

}
