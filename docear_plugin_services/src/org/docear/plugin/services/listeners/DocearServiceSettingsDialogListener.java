package org.docear.plugin.services.listeners;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;

import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.communications.features.AccountRegisterer;
import org.docear.plugin.communications.features.DocearServiceException;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.components.dialog.DocearIRChoiceDialogPanel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

public class DocearServiceSettingsDialogListener implements ActionListener {
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof DocearIRChoiceDialogPanel) {
			if(e.getID() == 0) {
				handleOkButton((DocearIRChoiceDialogPanel) e.getSource());
			}
			else {
				handleCancelButton((DocearIRChoiceDialogPanel) e.getSource());
			}
		}

	}
	
	private void handleCancelButton(DocearIRChoiceDialogPanel settings) {
		settings.close();		
	}

	private void handleOkButton(DocearIRChoiceDialogPanel settings) {
		try {
			checkAccountSettings(settings);
			Container cont = settings.getParent();
			while(!(cont instanceof JOptionPane)) {
				cont = cont.getParent();
			}
			((JOptionPane)cont).setValue(settings.getOkButton());
		} 
		catch (DocearServiceException e) {
			JOptionPane.showMessageDialog(settings, 
					TextUtils.getText("docear.uploadchooser.warning.notregistered")+e.getMessage(), 
					TextUtils.getText("docear.uploadchooser.warning.notregistered.title"), 
					JOptionPane.WARNING_MESSAGE);
			LogUtils.info("DocearServiceException: "+e.getMessage());
		} 
		catch (URISyntaxException e1) {
			JOptionPane.showMessageDialog(settings, TextUtils.getText("docear.uploadchooser.warning.notregistered"), TextUtils.getText("docear.uploadchooser.warning.notregistered.title"), JOptionPane.WARNING_MESSAGE);
			LogUtils.warn(e1);
		}
		
	}

	private boolean checkAccountSettings(DocearIRChoiceDialogPanel settings) throws DocearServiceException, URISyntaxException {
		int code = settings.getIrCode();
		if(code > 0 && !settings.allowBackup() && isEmpty(settings.getPassword())) {
			return true;
		}
		
		AccountRegisterer accountRegisterer = new AccountRegisterer();
		if (settings.useRegistration()) {
			if(!settings.allowBackup() && isEmpty(settings.getUserName()) && isEmpty(settings.getPassword()) && isEmpty(settings.getEmail())) {
				return true;
			}
			else if (isEmpty(settings.getPassword()) || isEmpty(settings.getEmail()) ) {
				throw new DocearServiceException(TextUtils.getText("docear.uploadchooser.warning.enterall"));
			}
			else {
				accountRegisterer.createRegisteredUser(settings.getUserName(), settings.getPassword(), settings.getEmail(), settings.getBirthYear(), settings.wantsNewsletter(), settings.isMale());
			}
		}
		else {		
			if (!isEmpty(settings.getUserName()) && !isEmpty(settings.getPassword())
					|| "".equals(ResourceController.getResourceController().getProperty("docear.service.connect.username","")) && !isEmpty(settings.getUserName())
					) {			
				CommunicationsController.getController().tryToConnect(settings.getUserName(), settings.getPassword(), true, false);
				
				
				if (ServiceController.getController().isBackupAllowed()) {
					return true;
				}
				else {
					return false;
				}
			}
			
		}
		
		if (code > 0) {
			//if user name is empty --> create anonymous user automatically when the information retrieval action runs 
			if (!isEmpty(settings.getUserName())) {
				if(!isEmpty(CommunicationsController.getController().getRegisteredAccessToken())) {
					return true;
				}
				if (isEmpty(settings.getPassword())) {
					//JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.uploadchooser.warning.nopassword"), TextUtils.getText("docear.uploadchooser.warning.nopassword.title"), JOptionPane.WARNING_MESSAGE);
					throw new DocearServiceException(TextUtils.getText("docear.uploadchooser.warning.nopassword"));
					//return false;
				}
				else {
					CommunicationsController.getController().tryToConnect(settings.getUserName(), settings.getPassword(), true, false);
					if (!isEmpty(CommunicationsController.getController().getRegisteredAccessToken())) {
						return true;
					}
					else {
						return false;
					}
				}
			}
			else {
				//if user name is empty --> create anonymous user automatically when the information retrieval action runs 
				return true;
			}
			
		}
		
		return true;
	}
	
	private boolean isEmpty(String s) {
		return s==null || s.trim().length()==0;
	}

}
