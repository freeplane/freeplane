package org.docear.plugin.communications;

public class Communication {
	private CommunicationsConfiguration config;
	private DocearAccount account;

	public Communication() {
		this.config = new CommunicationsConfiguration();
		this.account = new DocearAccount();
	}
	
	public CommunicationsConfiguration getConfig() {
		return config;
	}

	public void setConfig(CommunicationsConfiguration config) {
		this.config = config;
	}

	public DocearAccount getAccount() {
		return account;
	}

	public void setAccount(DocearAccount account) {
		this.account = account;
	}
	
	
}
