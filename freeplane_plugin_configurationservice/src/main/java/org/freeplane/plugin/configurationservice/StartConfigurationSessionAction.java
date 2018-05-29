package org.freeplane.plugin.configurationservice;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

class StartConfigurationSessionAction extends AFreeplaneAction {
	private static final String ACTION_NAME = "StartConfigurationSessionAction";
	private static final long serialVersionUID = 1L;

	private static final String mindMapFile = "C:\\Users\\Dimitry\\Desktop\\Files\\HelloWorld.mm";
//	private static final String mindMapFile = "C:\\neri\\mappementali\\HelloWorld.mm";
	private static int port =0;
	private ConfigurationSession configurationSession;

	private String clientSentence;

	public StartConfigurationSessionAction(ConfigurationSession configurationSession) {
		super(ACTION_NAME);
		this.configurationSession = configurationSession;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		configurationSession.start(mindMapFile);
		/*
		ServerSocket welcomeSocket;
		try {
			welcomeSocket = new ServerSocket(6789);
			  while (true) {
				   Socket connectionSocket = welcomeSocket.accept();
				   BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				   DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				   clientSentence = inFromClient.readLine();
				   LogUtils.info("Received: " + clientSentence);
				   outToClient.writeBytes("DONE");
				  }
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
	}
}
