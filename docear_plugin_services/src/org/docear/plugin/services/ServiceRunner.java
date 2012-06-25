package org.docear.plugin.services;

import java.io.File;

import org.docear.plugin.communications.CommunicationsController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;

public class ServiceRunner {
	final ServiceController backupCtrl;
	
	public ServiceRunner(ServiceController serviceController) {
		backupCtrl = serviceController;
	}

	public void run() {
		LogUtils.info("running Docear BackupRunner");

		final ResourceController resourceCtrl = Controller.getCurrentController().getResourceController();
		Thread thread = new Thread() {
			public void run() {
				while (true) {
					System.out.println("thread running");
					int backupMinutes = resourceCtrl.getIntProperty(
							"save_backup_automcatically", 0);
					if (backupMinutes <= 0) {
						backupMinutes = 30;
					}
					try {
						if (backupCtrl.isBackupAllowed() || backupCtrl.isInformationRetrievalAllowed()) {
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
					} catch (Exception e) {
						LogUtils.warn(e);
					}
					try {
						sleep(60000 * backupMinutes);
					} catch (InterruptedException e) {						
					}
				}

			}
		};
		thread.start();
	}

}