package org.docear.plugin.services;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;

import javax.swing.SwingUtilities;

import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.services.actions.DocearAllowUploadChooserAction;
import org.docear.plugin.services.features.UpdateCheck;
import org.docear.plugin.services.features.elements.Application;
import org.docear.plugin.services.listeners.DocearEventListener;
import org.docear.plugin.services.listeners.MapLifeCycleListener;
import org.docear.plugin.services.listeners.PropertiesActionListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.mode.Controller;

public class ServiceController {
	public static final String DOCEAR_INFORMATION_RETRIEVAL = "docear_information_retrieval";
	public static final String DOCEAR_SAVE_BACKUP = "docear_save_backup";

	private final static ServiceController serviceController = new ServiceController();
	
	private final ServiceRunner backupRunner = new ServiceRunner();
	private final File backupFolder = new File(CommunicationsController.getController().getCommunicationsQueuePath(), "mindmaps");
	
	private final IMapLifeCycleListener mapLifeCycleListener = new MapLifeCycleListener();
	public static final int ALLOW_RECOMMENDATIONS = 8;
	public static final int ALLOW_USAGE_MINING = 4;
	public static final int ALLOW_INFORMATION_RETRIEVAL = 2;
	public static final int ALLOW_RESEARCH = 1;
	
	private Application application;

	private static FileFilter zipFilter = new FileFilter() {
		public boolean accept(File f) {
			return (f != null && f.getName().toLowerCase().endsWith(".zip"));
		}		
	};
	
	public ServiceController() {
		LogUtils.info("starting DocearBackupStarter()");
		initListeners();
		
		new ServiceConfiguration();	    
		new ServicePreferences();
		
		addPluginDefaults();
		Controller.getCurrentModeController().addAction(new DocearAllowUploadChooserAction());
	
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {				
				backupRunner.run();
				new UpdateCheck();
			}		
		});
		
				
	}
	
	public void initListeners() {
		DocearController.getController().addDocearEventListener(new DocearEventListener());
		Controller.getCurrentModeController().getMapController().addMapLifeCycleListener(mapLifeCycleListener);		
		Controller.getCurrentController().getOptionPanelController().addButtonListener(new PropertiesActionListener());
	}
	
	public static ServiceController getController() {
		return serviceController;
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
		return ResourceController.getResourceController().getBooleanProperty(DOCEAR_SAVE_BACKUP);
	}
	
	public void setBackupEnabled(boolean b) {
		ResourceController.getResourceController().setProperty(DOCEAR_SAVE_BACKUP, b);
	}
	
	public int getInformationRetrievalCode() {
		return Integer.parseInt(ResourceController.getResourceController().getProperty(DOCEAR_INFORMATION_RETRIEVAL, "0"));
	}
	
	public boolean isResearchAllowed() {
		return (getInformationRetrievalCode() & ALLOW_RESEARCH) > 0;
	}
	
	public boolean isInformationRetrievalSelected() {
		return (getInformationRetrievalCode() & ALLOW_INFORMATION_RETRIEVAL) > 0;
	}
	
	public boolean isUsageMiningAllowed() {
		return (getInformationRetrievalCode() & ALLOW_USAGE_MINING) > 0;
	}
	
	public boolean isRecommendationsAllowed() {
		return (getInformationRetrievalCode() & ALLOW_RECOMMENDATIONS) > 0;
	}
	
	public void setInformationRetrievalCode(int code) {
		ResourceController.getResourceController().setProperty(DOCEAR_INFORMATION_RETRIEVAL, ""+code);
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
		boolean needUser = getInformationRetrievalCode()>0 && commCtrl.allowTransmission();
		
		return needUser && (!isEmpty(commCtrl.getAccessToken()) || !isEmpty(commCtrl.getUserName()));
	}
	
	private boolean isEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}
}
