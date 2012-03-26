package org.docear.plugin.backup;

import java.io.File;

import org.docear.plugin.communications.CommunicationsController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;

public class BackupRunner {

	public void run() {
		LogUtils.info("running Docear BackupRunner");

		final ResourceController resourceCtrl = Controller.getCurrentController().getResourceController();
		final BackupController backupCtrl = BackupController.getController();
		final CommunicationsController commCtrl = CommunicationsController.getController();

		Thread thread = new Thread() {
			public void run() {
				while (true) {
					System.out.println("thread running");
					try {
						int backupMinutes = resourceCtrl.getIntProperty(
								"save_backup_automcatically", 0);
						if (backupMinutes <= 0) {
							backupMinutes = 30;
						}

						if (backupCtrl.isBackupEnabled() && commCtrl.allowTransmission() && commCtrl.getAccessToken() != null && commCtrl.getAccessToken().trim().length() > 0) {
							LogUtils.info("Docear BackupRunner: synchronizing backups with server");
							File[] files = backupCtrl.getBackupQueue();
							if (files != null && files.length>0) {
								boolean success = CommunicationsController.getController().postFileToDocearService("mindmaps", true, files);
								if (success) {
									LogUtils.info("Docear BackupRunner: synchronizing successfull");
								}
								else {
									LogUtils.info("Docear BackupRunner: synchronizing failed");
								}
							}
							else {
								LogUtils.info("Docear BackupRunner: nothing to do");
							}
						}
						sleep(60000 * backupMinutes);
					} catch (Exception e) {
						LogUtils.warn(e);
					}
				}

			}
		};
		thread.start();
	}

}