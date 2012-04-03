package org.docear.plugin.services;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;

import javax.swing.SwingUtilities;

import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.services.actions.DocearAllowUploadChooserAction;
import org.docear.plugin.services.listeners.MapLifeCycleListener;
import org.docear.plugin.services.listeners.PropertiesActionListener;
import org.docear.plugin.services.listeners.PropertyListener;
import org.docear.plugin.services.listeners.WorkspaceListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;

public class ServiceController {
	private final static ServiceController backupController = new ServiceController();
	
	private final ServiceRunner backupRunner = new ServiceRunner();
	private final File backupFolder = new File(CommunicationsController.getController().getCommunicationsQueuePath(), "mindmaps");
	
	private final IMapLifeCycleListener mapLifeCycleListener = new MapLifeCycleListener();
	private final PropertyListener propertyListener = new PropertyListener();

	private static FileFilter zipFilter = new FileFilter() {
		public boolean accept(File f) {
			return (f != null && f.getName().toLowerCase().endsWith(".zip"));
		}		
	};
	
	public ServiceController() {
		LogUtils.info("starting DocearBackupStarter()");
		initListeners();
		
		addPluginDefaults();
		Controller.getCurrentModeController().addAction(new DocearAllowUploadChooserAction());
	
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {				
				backupRunner.run();			
			}		
		});
		
	}
	
	public void initListeners() {
		WorkspaceController.getController().addWorkspaceListener(new WorkspaceListener());
		Controller.getCurrentModeController().getMapController().addMapLifeCycleListener(mapLifeCycleListener);
		ResourceController.getResourceController().addPropertyChangeListener(propertyListener);
		Controller.getCurrentController().getOptionPanelController().addButtonListener(new PropertiesActionListener());
	}
	
	public static ServiceController getController() {
		return backupController;
	}
	
	public ServiceRunner getBackupRunner() {
		return backupRunner;
	}

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);		
	}
	
	public boolean isBackupEnabled() {
		return ResourceController.getResourceController().getBooleanProperty("docear_save_backup");
	}
	
	public void setBackupEnabled(boolean b) {
		ResourceController.getResourceController().setProperty("docear_save_backup", b);
	}
	
	public int getInformationRetrievalCode() {
		return Integer.parseInt(ResourceController.getResourceController().getProperty("docear_information_retrieval", "0"));
	}
	
	public void setInformationRetrievalCode(int code) {
		ResourceController.getResourceController().setProperty("docear_information_retrieval", ""+code);
	}
	
	public File getBackupDirectory() {		
		if (!backupFolder.exists()) {
			backupFolder.mkdirs();
		}
		return backupFolder;
	}
	
	
	public File[] getBackupQueue() {
		return getBackupDirectory().listFiles(zipFilter);
	}
	
	public boolean isBackupAllowed() {
		CommunicationsController commCtrl = CommunicationsController.getController();
		return isBackupEnabled() && commCtrl.allowTransmission() && !isEmpty(commCtrl.getRegisteredAccessToken()) && !isEmpty(commCtrl.getRegisteredUserName());
	}
	
	public boolean isInformationRetrievalAllowed() {
		CommunicationsController commCtrl = CommunicationsController.getController();
		boolean allowed = !isEmpty(commCtrl.getAccessToken()) || !isEmpty(commCtrl.getUserName());		
		return allowed && getInformationRetrievalCode()>0 && commCtrl.allowTransmission();
	}
	
	private boolean isEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}
}
