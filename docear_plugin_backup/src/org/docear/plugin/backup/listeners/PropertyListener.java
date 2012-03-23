package org.docear.plugin.backup.listeners;

import javax.swing.JOptionPane;

import org.docear.plugin.backup.BackupController;
import org.docear.plugin.communications.CommunicationsController;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

public class PropertyListener implements IFreeplanePropertyListener {

	public void propertyChanged(String propertyName, String newValue, String oldValue) {
		if ("docear_save_backup".equals(propertyName)) { 
		
			String docearConnectionToken = ResourceController.getResourceController().getProperty(CommunicationsController.DOCEAR_CONNECTION_TOKEN_PROPERTY);
			if (Boolean.parseBoolean(newValue)) {
				if (docearConnectionToken == null || docearConnectionToken.toLowerCase().trim().length()==0) {
					CommunicationsController.getController().showConnectionDialog();					
					resetBackupStatusIfNeeded();
				}
			}
		}
		else if (CommunicationsController.DOCEAR_CONNECTION_TOKEN_PROPERTY.equals(propertyName)) {			
			resetBackupStatusIfNeeded();
		}

	}

	private void resetBackupStatusIfNeeded() {
		String docearConnectionToken = ResourceController.getResourceController().getProperty(CommunicationsController.DOCEAR_CONNECTION_TOKEN_PROPERTY);
		if (docearConnectionToken == null || docearConnectionToken.toLowerCase().trim().length()==0) {
			JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.backup.error.no_token"), TextUtils.getText("docear.backup.error.no_token.title"), JOptionPane.ERROR_MESSAGE);
			((BooleanProperty)Controller.getCurrentController().getOptionPanelController().getPropertyControl("docear_save_backup")).setValue(false);
			BackupController.getController().setBackupEnabled(false);
		}
	}

}
