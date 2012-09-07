package org.docear.plugin.services.communications;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.docear.plugin.services.communications.components.dialog.ProxyAuthenticationDialog;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;

public class DocearAuthenticator extends Authenticator {
	protected PasswordAuthentication getPasswordAuthentication() {
		LogUtils.info(getRequestorType() + " (" + this.getRequestingHost() + ":" + this.getRequestingPort() + "): " +this.getRequestingPrompt() + " "+ this.getRequestingProtocol() + "/" + getRequestingScheme()+" for "+ getRequestingURL() +"   "+getRequestingSite());
		String username = ResourceController.getResourceController().getProperty(CommunicationsController.DOCEAR_PROXY_USERNAME);
		char[] password = CommunicationsController.password;
		if(username == null || password == null) {
			ProxyAuthenticationDialog dialog =  new ProxyAuthenticationDialog();
			dialog.showDialog();
			username = ResourceController.getResourceController().getProperty(CommunicationsController.DOCEAR_PROXY_USERNAME);
			password = CommunicationsController.password;
		}
    	return new PasswordAuthentication(username, password);
	}
}
