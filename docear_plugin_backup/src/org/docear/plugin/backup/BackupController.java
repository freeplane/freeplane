package org.docear.plugin.backup;

import java.io.File;
import java.net.URL;

import org.docear.plugin.backup.listeners.MapLifeCycleListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.mode.Controller;

public class BackupController {
	private final static BackupController backupController = new BackupController();
	
	private final BackupRunner backupRunner = new BackupRunner();
	private final File backupFolder = new File(ResourceController.getResourceController().getFreeplaneUserDirectory() + File.separator + "backup");
	
	private final IMapLifeCycleListener mapLifeCycleListener = new MapLifeCycleListener();

	public BackupController() {
		LogUtils.info("starting DocearBackupStarter()");		
		Controller.getCurrentModeController().getMapController().addMapLifeCycleListener(mapLifeCycleListener);
		
		addPluginDefaults();
		
		if (this.isBackupEnabled()) {
			this.backupRunner.run();
		}
	}
	
	public static BackupController getController() {
		return backupController;
	}
	
	public BackupRunner getBackupRunner() {
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
	
	public File getBackupDirectory() {		
		if (!backupFolder.exists()) {
			backupFolder.mkdirs();
		}
		return backupFolder;
	}
}
