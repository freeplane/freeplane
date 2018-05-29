package org.freeplane.plugin.configurationservice;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;

class UpdateConfigurationAction extends AFreeplaneAction {
	private static final String ACTION_NAME = "UpdateConfigurationAction";
	private static final long serialVersionUID = 1L;
	private ConfigurationSession configurationSession;
	public UpdateConfigurationAction(ConfigurationSession configurationSession ) {
		
		super(ACTION_NAME);
		this.configurationSession = configurationSession;
	}

	public void actionPerformed(final ActionEvent e) {
		configurationSession.update("ID_1053277958", "a", 45);

	}
}
