package org.docear.plugin.backup;

import org.docear.plugin.communications.CommunicationsController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.jdesktop.swingworker.SwingWorker;


public class BackupRunner {
	private SwingWorker<Void, Void> runner;
	private boolean running = false;

	public void run() {
		if (running) {
			return;
		}
		
		LogUtils.info("running Docear BackupRunner");
		
		running = true;
		
		final ResourceController resourceController = Controller.getCurrentController().getResourceController();		
		
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		
		runner = new SwingWorker<Void, Void>() {
			public Void doInBackground() {				
				while (true) {
					synchronized (this) {
						int backupMinutes = resourceController.getIntProperty("save_backup_automcatically", 0);
						if (backupMinutes <= 0) {
							backupMinutes = 30;
						}
						try {														
							if (BackupController.getController().isBackupEnabled()) {
								LogUtils.info("Docear BackupRunner: synchronizing backups with server");
								boolean success = CommunicationsController.getController().postFileToDocearService("mindmaps", true, BackupController.getController().getBackupBufferFiles());								
								if (success) {
									LogUtils.info("Docear BackupRunner: synchronizing successfull");
								}
								else {
									LogUtils.info("Docear BackupRunner: synchronizing failed");
								}
							}
							this.wait(60000 * backupMinutes);
						}
						catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		Thread.currentThread().setContextClassLoader(contextClassLoader);
		
		runner.execute();
	}
	
	public void stop() {
		LogUtils.info("stoping Docear BackupRunner");
		
		runner.cancel(true);
		running = false;
	}

}