package org.docear.plugin.pdfutilities.actions;

import java.awt.event.ActionEvent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.Controller;

public class RadioButtonAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String propertyKey;
	

	public RadioButtonAction(String key, String propertyKey) {
		super(key);
		this.propertyKey = propertyKey;
	}

	public void actionPerformed(ActionEvent arg0) {
		ResourceController resourceController = Controller.getCurrentController().getResourceController();
		resourceController.setProperty(this.propertyKey, !resourceController.getBooleanProperty(this.propertyKey));
	}

}
