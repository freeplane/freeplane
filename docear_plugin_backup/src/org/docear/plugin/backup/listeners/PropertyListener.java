package org.docear.plugin.backup.listeners;

import org.docear.plugin.backup.BackupController;
import org.freeplane.core.resources.IFreeplanePropertyListener;

public class PropertyListener implements IFreeplanePropertyListener {

	public void propertyChanged(String propertyName, String newValue,
			String oldValue) {
		BackupController backupController = BackupController.getController();
		
		if (propertyName.equals("docear_save_backup")) {
			if (backupController.isBackupEnabled()) {
				backupController.getBackupRunner().run();
			}
			else {
				backupController.getBackupRunner().stop();
			}
		}
	}

}
