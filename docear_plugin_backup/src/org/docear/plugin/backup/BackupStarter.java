package org.docear.plugin.backup;

import java.io.StringWriter;
import java.net.URL;

import org.docear.plugin.communications.CommunicationsConfiguration;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class BackupStarter implements IMapChangeListener, INodeChangeListener {

	private CommunicationsConfiguration config = new CommunicationsConfiguration();
	private BackupRunner backupRunner;

	BackupStarter() {
		LogUtils.info("starting DocearBackupStarter()");
		Controller.getCurrentModeController().getMapController().addMapChangeListener(this);
		Controller.getCurrentModeController().getMapController().addNodeChangeListener(this);
		addPluginDefaults();
		
		this.backupRunner = new BackupRunner(config);
	}

	private void addPluginDefaults() {
		final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		if (defaults == null)
			throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
		Controller.getCurrentController().getResourceController().addDefaults(defaults);
	}

	public void mapChanged(MapChangeEvent event) {
		LogUtils.info("Docear: mapChangedEvent");
		StringWriter sw = new StringWriter();
		Controller controller = Controller.getCurrentController();

		try {
			if (!controller.getResourceController().getProperty("docear_save_backup").equals("true")) {
				return;
			}
			//this.backupRunner.backup();

			System.out.println("docear_save_backup="
					+ Controller.getCurrentModeController().getController().getResourceController()
							.getProperty("docear_save_backup"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(sw.toString());
	}


	public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
		this.backupRunner.setMapChanged(true);
		LogUtils.info("Docear: onNodeDeletedEvent");
	}

	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
		this.backupRunner.setMapChanged(true);
		LogUtils.info("Docear: onNodeInsertedEvent");
	}

	public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
		this.backupRunner.setMapChanged(true);
		LogUtils.info("Docear: onNodeMovedEvent");
	}

	public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
		LogUtils.info("Docear: onPreNodeMovedEvent");
	}

	public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode, int index) {
		LogUtils.info("Docear: onPreNodeDeleteEvent");
	}
	
	public void nodeChanged(NodeChangeEvent event) {
		LogUtils.info("Docear: nodeChangedEvent");
		if (event.getOldValue() != null) {			
			this.backupRunner.setMapChanged(true);
		}
	}

}
