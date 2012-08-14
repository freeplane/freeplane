package org.docear.plugin.services.upload;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.docear.plugin.core.features.DocearThread;
import org.docear.plugin.core.io.DirectoryObserver;
import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.services.communications.CommunicationsController;
import org.docear.plugin.services.communications.FiletransferClient;

public class UploadThread extends DocearThread implements DirectoryObserver {
	
	private final UploadController uploadCtrl;
	
	private Set<File> uploadFiles = new HashSet<File>();
	
	public UploadThread(UploadController controller) {
		super("Docear Upload-Thread");
		uploadCtrl = controller;
		uploadCtrl.addUploadDirectoryObserver(this);
		loadOldFiles();
	}
	
	private void loadOldFiles() {
		File[] files = uploadCtrl.getUploadPackages();
		if(files == null) {
			return;
		}
		for(File file : files) {
			fileCreated(file);
		}
	}

	public void execute() {
		while (!isTerminated()) {
			DocearLogger.info(this+" running.");
			int backupMinutes = uploadCtrl.getUploadInterval();
			try {
				if (uploadCtrl.isBackupAllowed() || uploadCtrl.isInformationRetrievalAllowed()) {
					DocearLogger.info(this.toString()+": uploading packages to the server ...");
					File[] files = uploadFiles.toArray(new File[]{}); //uploadCtrl.getUploadPackages();
					if (files != null && files.length>0) {
						FiletransferClient client = CommunicationsController.getController().getFileTransferClient("mindmaps");
						for(File file : files) {
							//boolean success = CommunicationsController.getController().postFileToDocearService("mindmaps", file, true);
							if(!file.exists()) {
								fileRemoved(file);
								continue;
							}
							boolean success = client.sendFile(file, true);
							if (success) {
								DocearLogger.info(this.toString()+": synchronizing '"+file+"' successfull");
								fileRemoved(file);
							}
							else {
								DocearLogger.info(this.toString()+": synchronizing '"+file+"' failed");
							}
						}
					}
					else {
						DocearLogger.info(this.toString()+": nothing to do");
					}
				}
			} catch (Exception e) {
				DocearLogger.warn("org.docear.plugin.services.upload.UploadThread.execute(): "+e.getMessage());
			}
			try {
				if(!isInterrupted()) {
					sleep(60000 * backupMinutes);
				}
			} catch (InterruptedException e) {
			}
		}

	}

	public void fileCreated(File file) {
		synchronized (uploadFiles) {
			uploadFiles.add(file);
		}
	}

	public void fileRemoved(File file) {
		synchronized (uploadFiles) {
			uploadFiles.remove(file);
		}
	}
	

}
