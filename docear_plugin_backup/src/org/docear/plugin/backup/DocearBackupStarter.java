package org.docear.plugin.backup;

import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;

import javax.swing.JOptionPane;

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
				validateOptions();
			}
			System.out.println("docear_save_backup="+Controller.getCurrentModeController().getController().getResourceController().getProperty("docear_save_backup"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(sw.toString());		
	}
	
	private IValidator optionValidator;
	
	public void validateOptions() {		
		this.setOptionValidator(new IValidator() {
			public ValidationResult validate(Properties properties) {
				properties.list(System.out);
				final ValidationResult result = new ValidationResult();
				
				final String backup_server_path = properties
						.getProperty(BackupOptions.RESOURCES_BACKUP_SERVER_PATH);
				Integer connection_timeout = 0;
				try {
					connection_timeout = Integer.parseInt(properties
							.getProperty(BackupOptions.RESOURCES_CONNECTION_TIMEOUT));
				}catch(NumberFormatException e) {
					connection_timeout = 0;
				}
				
				if (backup_server_path == null || backup_server_path.length() == 0) {
					result.addError("backup_server_url_not_defined");
				}
				if (connection_timeout == 0) {
					result.addWarning("connection_timeout_not_defined");
				}
				
				return result;
			}
		});
		
		final ResourceController resourceController = Controller.getCurrentController().getResourceController();
		ValidationResult result = getOptionValidator().validate(resourceController.getProperties());
		if (result.isValid()) {
			Backup backup = new Backup();
			backup.doBackup();
		}
		else {
			String message = TextUtils.getText("critical_error");
			for (String s : result.getErrors()) {
				message += "\n"+TextUtils.getText(s);
			}
			
			JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getJFrame(), message);
			LogUtils.info("DOCEAR: hasErrors");
		}		
		
	}
	
	private IValidator getOptionValidator() {
	    return this.optionValidator;
    }

	private void setOptionValidator(IValidator validator) {
		this.optionValidator = validator;
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
