package org.docear.plugin.services.communications.components.dialog;

import javax.swing.JOptionPane;

import org.docear.plugin.services.communications.CommunicationsController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;

public class ProxyAuthenticationDialog {
	
	boolean okSelected = false;
	
	public boolean isOKselected() {
		return okSelected;
	}

	public void showDialog() {
		showDialog(false);
	}
	public void showDialog(boolean forced){
		if(!CommunicationsController.isProxyCanceled() || forced) {
			ProxyAuthenticationPanel panel = new ProxyAuthenticationPanel();
			boolean wasSelected = panel.getChckbxUseProxy().isSelected();
			int result = JOptionPane.showConfirmDialog(null, panel, TextUtils.getText("docear.proxy.connect.dialog.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if(result == JOptionPane.OK_OPTION){
				ResourceController.getResourceController().setProperty(CommunicationsController.DOCEAR_USE_PROXY, panel.getChckbxUseProxy().isSelected());
				ResourceController.getResourceController().setProperty(CommunicationsController.DOCEAR_PROXY_HOST, panel.getHostField().getText());
				ResourceController.getResourceController().setProperty(CommunicationsController.DOCEAR_PROXY_PORT, panel.getPortField().getText());
				ResourceController.getResourceController().setProperty(CommunicationsController.DOCEAR_PROXY_USERNAME, panel.getUsernameField().getText());
				CommunicationsController.setPassword(panel.getPasswordField().getPassword());
				CommunicationsController.updateClientConfiguration(wasSelected!=panel.getChckbxUseProxy().isSelected());
				okSelected = true;
				CommunicationsController.setProxyCanceled(false);
				return;
			}
			CommunicationsController.setProxyCanceled(true);
			okSelected = false;	
		}
	}	

}
