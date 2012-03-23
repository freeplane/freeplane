package org.docear.plugin.backup.listeners;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.SwingWorker;

import org.docear.plugin.backup.BackupController;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.features.DocearMapModelExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapModel;

public class MapLifeCycleListener implements IMapLifeCycleListener {

	public void onCreate(MapModel map) {
	}

	public void onRemove(MapModel map) {
	}

	public void onSavedAs(MapModel map) {
	}

	public void onSaved(MapModel map) {
		try {
			createBackup(map);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Properties getMapProperties(MapModel map) {
		DocearController docearController = DocearController.getController();
		
		DocearMapModelExtension dmme = map.getExtension(DocearMapModelExtension.class);
		if (dmme == null) {
			return null;
		}		
		Properties properties = new Properties();
		properties.put("mindmap_id", dmme.getMapId());
		properties.put("timestamp", ""+System.currentTimeMillis());
		properties.put("backup", new Boolean(true).toString());
		properties.put("allow_ir", new Boolean((ResourceController.getResourceController().getBooleanProperty("docear_allow_information_retrieval"))).toString());
		properties.put("map_version", dmme.getVersion());
		properties.put("application_name", docearController.getApplicationName());
		properties.put("application_version", docearController.getApplicationVersion());
		properties.put("application_status", docearController.getApplicationStatus());
		properties.put("application_status_version", docearController.getApplicationStatusVersion());
		properties.put("application_build", ""+docearController.getApplicationBuildNumber());
		properties.put("application_date", docearController.getApplicationBuildDate());
		properties.put("filesize", ""+map.getFile().length());
		properties.put("filename", map.getFile().getName());
		properties.put("filepath", map.getFile().getAbsolutePath());
		
		return properties;
	}

	public void createBackup(final MapModel map) throws IOException {
		if (map == null || !BackupController.getController().isBackupEnabled()) {
			return;
		}
		
		final Properties meta = getMapProperties(map);
		if (meta == null) {
			return;
		}
		
		Thread thread = new Thread() {
			public void run() {
				try {
					File backupFile = new File(BackupController.getController().getBackupDirectory().getAbsolutePath(), System.currentTimeMillis() + "_" + map.getFile().getName() + ".zip");
					
					ZipOutputStream out = null;
					InputStream in = null;
					try {			
						out = new ZipOutputStream(new FileOutputStream(backupFile));
						in = new FileInputStream(map.getFile());
						
						ZipEntry entry = new ZipEntry("metadata.inf");
						out.putNextEntry(entry);
						meta.store(out, "");
						
						entry = new ZipEntry(map.getFile().getName());			
						out.putNextEntry(entry);
						
						while (true) {
							int data = in.read();
							if (data == -1) {
								break;
							}
							out.write(data);
						}
						
					} 
					finally {
						in.close();
						out.close();			
					}					
				}
				catch (Exception e) {
					LogUtils.warn(e);
				}
				
			}
			
		};
		
		thread.run();
		
	}

}
