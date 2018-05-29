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
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;

class StartConfigurationSessionAction extends AFreeplaneAction {
	private static final String ACTION_NAME = "StartConfigurationSessionAction";
	private static final long serialVersionUID = 1L;
	private static final String mindMapFile = "C:\\neri\\mappementali\\HelloWorld.mm";
	private static int port =0;
	private ConfigurationSession configurationSession;
	private String clientSentence;
	
	public StartConfigurationSessionAction(ConfigurationSession configurationSession) {
		super(ACTION_NAME);
		this.configurationSession = configurationSession;
	}

	public void actionPerformed(final ActionEvent e) {
		
		configurationSession.start(mindMapFile);
		//startTCPListener();
		
	}
	
	private void startTCPListener() {
		
		ServerSocket welcomeSocket;
		try {
			welcomeSocket = new ServerSocket(6789);
			  while (true) {
				   Socket connectionSocket = welcomeSocket.accept();
				   BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				   DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				   clientSentence = inFromClient.readLine();
				   LogUtils.info("Received: " + clientSentence);
				   
				   if(clientSentence == "DOIT") {
						configurationSession.update("ID_1053277958", "a", 45);
						List<String> attributesList = new ArrayList<>();
						attributesList.add("a");
						attributesList.add("b");
						attributesList.add("area");
						
						Map<String, Object> attributeMap = configurationSession.readValues("ID_1053277958", attributesList);
	
						for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
							LogUtils.info(entry.getKey() + " " + entry.getValue());
						}
						
				   }
				   LogUtils.info("Sending respone");
				   //outToClient.writeBytes("DONE");
				   
				  }
		} catch (IOException e1) {
			LogUtils.info(e1.getMessage());
		}
	}
}
