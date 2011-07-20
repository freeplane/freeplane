package org.docear.plugin.backup;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.docear.plugin.communications.CommunicationsConfiguration;
import org.docear.plugin.communications.Filetransfer;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.accountmanager.Account;

public class BackupRunner {
	//TODO: inserting the mindmapId into the mindmapmodel fires an mapchanged event --> map is automatically updated on server
	
	private final static String AUTO_BACKUP_MINUTES = "save_backup_automcatically";
	private boolean mapChanged = false;

	private CommunicationsConfiguration config;

	public BackupRunner(CommunicationsConfiguration config) {
		this.config = config;
		autoBackup();
	}

	public boolean isMapChanged() {
		return mapChanged;
	}

	public void setMapChanged(boolean mapChanged) {
		this.mapChanged = mapChanged;
	}

	private void autoBackup() {
		final ResourceController resourceController = Controller.getCurrentController().getResourceController();
		final int auto_backup_minutes = resourceController.getIntProperty(AUTO_BACKUP_MINUTES);

		if (auto_backup_minutes <= 0) {
			return;
		}

		SwingWorker<Void, Void> runner = new SwingWorker<Void, Void>() {
			public Void doInBackground() {
				while (true) {
					synchronized (this) {
						try {
							System.out.println("TEST");
							this.wait(60000 * auto_backup_minutes);
						}
						catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if (isMapChanged()) {
						setMapChanged(false);
						System.out.println("change map");
						Filetransfer.copyMindmapToServer(config);
					}
				}
			}
		};
		runner.execute();

	}

	public void backup() {
		Account account = config.getAccount();
		if (!account.hasUsername() || !account.hasPassword() || !account.hasConnectionString()) {
			JOptionPane.showMessageDialog(null, TextUtils.getText("account_credentials_not_found"), "error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			SwingWorker<Void, Void> runner = new SwingWorker<Void, Void>() {
				public Void doInBackground() {
					setMapChanged(false);
					Filetransfer.copyMindmapToServer(config);
					return null;
				}
			};
			runner.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

}