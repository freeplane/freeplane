package org.freeplane.plugin.configurationservice;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

class StartConfigurationSessionAction extends AFreeplaneAction {
	private static final String ACTION_NAME = "StartConfigurationSessionAction";
	private static final long serialVersionUID = 1L;

	private ConfigurationSession configurationSession;


	public StartConfigurationSessionAction(ConfigurationSession configurationSession) {
		super(ACTION_NAME);
		this.configurationSession = configurationSession;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		String mindMapFile = System.getProperty("freeplane.configurationservice.mindMapFile");
		System.out.println("MM: " + mindMapFile);
		configurationSession.start(mindMapFile);
	}
}
