package org.docear.plugin.services.upload;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.features.DocearMapModelExtension;
import org.docear.plugin.core.io.DirectoryObserver;
import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.communications.CommunicationsController;
import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public abstract class UploadController {
	private final File uploadBufferDirectory = new File(CommunicationsController.getController().getCommunicationsQueuePath(), "mindmaps");
	
	private static FileFilter zipFilter = new FileFilter() {
		public boolean accept(File f) {
			return (f != null && f.getName().toLowerCase().endsWith(".zip"));
		}
	};
	
	private final Map<String, MapModel> mapUploadQueue = new HashMap<String, MapModel>();
	
	private final Set<DirectoryObserver> observers = new HashSet<DirectoryObserver>(); 
	
	private final Runnable packerRunner = new Runnable() {
		public void run() {
			createPackages();
		}
	};
	
	private final CyclicUploadPacker packerThread = new CyclicUploadPacker(packerRunner, (180)); //every 3 minutes 
	
	private final UploadThread uploadThread = new UploadThread(this);
	
	
	/**
	 * @return
	 */
	public abstract boolean isInformationRetrievalAllowed();

	/**
	 * @return
	 */
	public abstract boolean isBackupAllowed();
	
	/**
	 * Provides the time in minutes until the next upload cycle should be started
	 * 
	 * @return time to wait until the next upload cycle
	 */
	public abstract int getUploadInterval();

	/**
	 * @return
	 */
	public File[] getUploadPackages() {
		return getUploadDirectory().listFiles(zipFilter);
	}

	/**
	 * @return
	 */
	public File getUploadDirectory() {
		if (!uploadBufferDirectory.exists()) {
			uploadBufferDirectory.mkdirs();
		}
		return uploadBufferDirectory;
	}
	
	/**
	 * @param map
	 */
	public void addMapToUpload(MapModel map) {
		boolean backup = isBackupAllowed();
		boolean ir = isInformationRetrievalAllowed();
		File file = map.getFile();
		if(file == null || (!backup && !ir)) {
			return;
		}
		DocearMapModelExtension mapExt = map.getExtension(DocearMapModelExtension.class);
		if(mapExt != null && mapExt.getMapId() != null) {
			synchronized (mapUploadQueue) {
				mapUploadQueue.put(mapExt.getMapId(), map);
			}
		}
		
	}
	
	/**
	 * @param observer
	 */
	public void addUploadDirectoryObserver(DirectoryObserver observer) {
		synchronized (observers) {
			observers.add(observer);
		}		
	}
	
	/**
	 * @param observer
	 */
	public void removeUploadDirectoryObserver(DirectoryObserver observer) {
		synchronized (observers) {
			observers.remove(observer);
		}		
	}
	
	/**
	 * @return
	 */
	protected Thread getPacker() {
		return this.packerThread;
	}
	
	/**
	 * @return
	 */
	protected Thread getUploader() {
		return this.uploadThread;
	}
	
	/**
	 * 
	 */
	private void createPackages() {
		synchronized (mapUploadQueue) {
			Iterator<Entry<String, MapModel>> iter = mapUploadQueue.entrySet().iterator();
			while(iter.hasNext()) {
				Entry<String, MapModel> entry = iter.next();
				createMapPackage(entry.getValue());
				iter.remove();				
			}
		}		
	}
	
	/**
	 * @param file
	 */
	protected final void fireFileCreated(File file) {
		synchronized (observers) {
			for(DirectoryObserver observer : observers) {
				observer.fileCreated(file);
			}
		}
	}
	
	/**
	 * @param file
	 */
	protected final void fireFileRemoved(File file) {
		synchronized (observers) {
			for(DirectoryObserver observer : observers) {
				observer.fileRemoved(file);
			}
		}
	}

	/**
	 * @param map
	 */
	private void createMapPackage(final MapModel map) {		
		if (map == null) {
			return;
		}
		
		final Properties meta = getMapProperties(map);
		if (meta == null) {
			return;
		}
		
		Thread thread = new Thread() {
			public void run() {
				try {					
					File backupFile = new File(getUploadDirectory().getAbsolutePath(), System.currentTimeMillis() + "_" + map.getFile().getName() + ".zip");
					
					
					FileOutputStream fout = null;
					ZipOutputStream out = null;					
					InputStream in = null;
					try {			
						fout = new FileOutputStream(backupFile);
						out = new ZipOutputStream(fout);
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
						out.flush();
					} 
					finally {	
						in.close();
						out.close();
						fout.close();
						fireFileCreated(backupFile);
						
						DocearController.getController().removeWorkingThreadHandle(this.getName());
					}					
				}
				catch (Exception e) {
					DocearLogger.warn("org.docear.plugin.services.upload.UploadController.createMapPackage(): "+e.getMessage());
				}				
			}
			
		};
		DocearController.getController().addWorkingThreadHandle(thread.getName());
		thread.start();
	}
	
	/**
	 * @param map
	 * @return
	 */
	private Properties getMapProperties(MapModel map) {
		ServiceController serviceController = ServiceController.getController();
		DocearController docearController = DocearController.getController();
				
		DocearMapModelExtension dmme = map.getExtension(DocearMapModelExtension.class);
		if (dmme == null) {
			return null;
		}
		
		boolean isLibraryMap = false;
		for (URI uri : DocearController.getController().getLibrary().getMindmaps()) { 
			if (uri != null && map != null) {
				String path = map.getFile().getAbsolutePath(); 
				File f = WorkspaceUtils.resolveURI(uri);
				
				if (f != null && f.getAbsolutePath().equals(path)) {
					isLibraryMap = true;
				}
			}
		}
		
		String typeName = (dmme.getType() == null ? "" : dmme.getType().name());
		
		Properties properties = new Properties();
		properties.put("mindmap_id", dmme.getMapId());
		properties.put("timestamp", ""+System.currentTimeMillis());
		properties.put("is_library_map", new Boolean(isLibraryMap).toString());
		properties.put("backup", new Boolean(serviceController.isBackupAllowed()).toString());
		properties.put("allow_content_research", new Boolean(serviceController.isResearchAllowed()).toString());
		properties.put("allow_information_retrieval", new Boolean(serviceController.isInformationRetrievalSelected()).toString());		
		properties.put("allow_usage_research", new Boolean(serviceController.isUsageMiningAllowed()).toString());
		properties.put("allow_recommendations", new Boolean(serviceController.isRecommendationsAllowed()).toString());
		
		if (typeName != null && typeName.trim().length()>0) {
			properties.put("map_type", typeName);
		}		
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
	
	/**
	 * 
	 */
	public void finishThreads() {		
		String runnerID = Integer.toHexString(this.hashCode());
		DocearController.getController().addWorkingThreadHandle(runnerID);
		packerRunner.run();
		DocearController.getController().removeWorkingThreadHandle(runnerID);
	}
	
	public void shutdown() {		
		this.packerThread.terminate();
		this.uploadThread.terminate();
	}
}
