package org.freeplane.plugin.configurationservice;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;

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
