package org.docear.plugin.backup;

import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.docear.plugin.communications.CommunicationsConfiguration;
import org.docear.plugin.communications.Filetransfer;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.plugin.accountmanager.Account;
import org.jdesktop.swingworker.SwingWorker;


public class BackupRunner {
	// TODO: inserting the mindmapId into the mindmapmodel fires an mapchanged
	// event --> map is automatically updated on server

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

		LogUtils.info("org.docear.plugin.backup: automatic backup every " + auto_backup_minutes + " minutes.");

		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		SwingWorker<Void, Void> runner;
		runner = new SwingWorker<Void, Void>() {
			public Void doInBackground() {
				while (true) {
					synchronized (this) {
						try {
							this.wait(60000 * auto_backup_minutes);
							Account account = config.getAccount();
							if (config.isBackup() && isMapChanged() && account.hasUsername() && account.hasPassword() && account.hasConnectionString()) {
								for (Entry<String, MapModel> entry : Controller.getCurrentController().getMapViewManager().getMaps().entrySet()) {
									save(entry.getValue());
									//save event automatically triggers backup
									//backup(entry.getValue());
								}
							}
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
	
	public void save(final MapModel map) {
		((MFileManager) UrlManager.getController()).save(map, false);
	}
	
	public void backup() {
		backup(Controller.getCurrentController().getMap());
	}

	public void backup(final MapModel map) {
		Account account = config.getAccount();
		if (config.isBackup() && !account.hasUsername() || !account.hasPassword() || !account.hasConnectionString()) {
			JOptionPane.showMessageDialog(null, TextUtils.getText("account_credentials_not_found"), "error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			SwingWorker<Void, Void> runner = new SwingWorker<Void, Void>() {
				public Void doInBackground() {
					System.out.println("debug: backup mindmap");
					Filetransfer.copyMindmapToServer(config, map);
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