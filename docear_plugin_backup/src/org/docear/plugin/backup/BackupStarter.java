package org.docear.plugin.backup;

import java.awt.EventQueue;
import java.io.StringWriter;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.docear.plugin.communications.CommunicationsConfiguration;
import org.docear.plugin.communications.Filetransfer;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.ui.ViewController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.plugin.accountmanager.Account;

public class BackupStarter implements IMapChangeListener {

	public final static String MINDMAP_ID = "mindmapId";

	private CommunicationsConfiguration config = new CommunicationsConfiguration();

	BackupStarter() {
		LogUtils.info("starting DocearBackupStarter()");
		Controller.getCurrentModeController().getMapController().addMapChangeListener(this);
		addPluginDefaults();
	}

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}

	public void mapChanged(MapChangeEvent event) {
		LogUtils.info("Docear: mapChanged");
		StringWriter sw = new StringWriter();
		Controller controller = Controller.getCurrentController();

		try {
			if (!controller.getResourceController().getProperty("docear_save_backup").equals("true")) {
				return;
			}
			backup();

			System.out.println("docear_save_backup="
					+ Controller.getCurrentModeController().getController().getResourceController()
							.getProperty("docear_save_backup"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(sw.toString());
	}

	private void backup() {
		Account account = config.getAccount();
		System.out.println("account.hasUsername: " + (account.hasUsername() ? "true" : "false"));
		System.out.println("account.hasPassword: " + (account.hasPassword() ? "true" : "false"));
		System.out.println("account.hasConnectionString: " + (account.hasConnectionString() ? "true" : "false"));

		System.out.println(account);

		if (!account.hasUsername() || !account.hasPassword() || !account.hasConnectionString()) {
			JOptionPane.showMessageDialog(null, TextUtils.getText("account_credentials_not_found"), "error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		Controller controller = Controller.getCurrentController();
		ModeController modeController = Controller.getCurrentModeController();

		final MapStyleModel styleModel = MapStyleModel.getExtension(controller.getMap());
		String mindmapId = styleModel.getProperty(MINDMAP_ID);

		StringWriter sw = new StringWriter();

		try {
			modeController.getMapController().getFilteredXml(controller.getMap(), sw, Mode.EXPORT, true);
			String filename = controller.getMap().getFile().getName();

			String xml = sw.toString();

			if (mindmapId == null || mindmapId.isEmpty()) {
				System.out.println("org.docear.plugin.backup: inserting new mindmap: " + filename);
				insert(xml, filename);
			}
			else {
				System.out.println("org.docear.plugin.backup: updating mindmap: " + filename + " (ID: " + mindmapId + ")");
				update(mindmapId, xml, filename);
			}

			boolean saved = ((MFileManager) UrlManager.getController()).save(controller.getMap());

			System.out.println("debug save: " + (saved ? "true" : "false"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void insert(final String xml, final String filename) {
		SwingWorker<Void, Void> runner = new SwingWorker<Void, Void>() {

			public Void doInBackground() {
				final ViewController viewController = Controller.getCurrentController().getViewController();
				viewController.addStatusInfo("org.docear.plugin.backup", TextUtils.getText("backup_saved"), null);
				
				final Controller controller = Controller.getCurrentController();
				final ModeController modeController = Controller.getCurrentModeController();

				String mindmap_id;
				try {
					mindmap_id = Filetransfer.insertMindmap(config, xml, filename);
					MapStyle mapStyle = (MapStyle) modeController.getExtension(MapStyle.class);
					mapStyle.setProperty(controller.getMap(), MINDMAP_ID, mindmap_id);
					((MFileManager) UrlManager.getController()).save(controller.getMap());
				}
				catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, TextUtils.getText("error_backup_insert"), "error",
							JOptionPane.ERROR_MESSAGE);
				}
				viewController.removeStatus("org.docear.plugin.backup");
				return null;
			}
		};

		runner.execute();

	}

	private void update(final String mindmapId, final String xml, final String filename) {
		SwingWorker<Void, Void> runner = new SwingWorker<Void, Void>() {

			public Void doInBackground() {
				final ViewController viewController = Controller.getCurrentController().getViewController();
				viewController.addStatusInfo("org.docear.plugin.backup", TextUtils.getText("backup_saved"), null);

				try {
					Filetransfer.updateMindmap(config, xml, mindmapId, filename);
				}
				catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, TextUtils.getText("error_backup_update"), "error",
							JOptionPane.ERROR_MESSAGE);
				}
				viewController.removeStatus("org.docear.plugin.backup");
				return null;
			}
		};
		runner.execute();

	}

	public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
		// TODO Auto-generated method stub
		LogUtils.info("Docear: onNodeDeleted");
	}

	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		// TODO Auto-generated method stub
		LogUtils.info("Docear: onNodeInserted");
	}

	public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
		// TODO Auto-generated method stub
		LogUtils.info("Docear: onNodeMoved");
	}

	public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
		// TODO Auto-generated method stub
		LogUtils.info("Docear: onPreNodeMoved");
	}

	public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode, int index) {
		// TODO Auto-generated method stub
		LogUtils.info("Docear: onPreNodeDelete");
	}

}
