package org.docear.plugin.backup;

import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.docear.plugin.communications.CommunicationsConfiguration;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.IValidator;
import org.freeplane.core.resources.components.IValidator.ValidationResult;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class DocearBackupStarter implements IMapChangeListener {
	
	private CommunicationsConfiguration config = new CommunicationsConfiguration();
	
	DocearBackupStarter() {
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
		try {
			if (Controller.getCurrentModeController().getController().getResourceController().getProperty("docear_save_backup").equals("true")) {
				config.validateUserData();
			}
			System.out.println("docear_save_backup="+Controller.getCurrentModeController().getController().getResourceController().getProperty("docear_save_backup"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(sw.toString());		
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
