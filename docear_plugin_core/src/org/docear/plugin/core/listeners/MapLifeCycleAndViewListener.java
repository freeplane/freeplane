package org.docear.plugin.core.listeners;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.features.DocearMapModelExtension;
import org.docear.plugin.core.features.DocearMapModelExtension.DocearMapType;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.docear.plugin.core.ui.MapIdsConflictsPanel;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.url.UrlManager;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class MapLifeCycleAndViewListener implements IMapLifeCycleListener, IMapViewChangeListener {

	public void onCreate(MapModel map) {
		if (map instanceof MMapModel) {
			File f = map.getFile();
			if (f!=null) {
				showMapIdConflictingDialogIfNeeded(map);
				
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.MAP_OPENED, f);
				File installWelcomeMap = new File(System.getProperty("user.dir"), "doc" + File.separator + "docear-welcome.mm");
				File userdirWelcomeMap = new File(WorkspaceUtils.getDataDirectory(), "help" + File.separator + "docear-welcome.mm");				
				if(f.equals(installWelcomeMap) || f.equals(userdirWelcomeMap)){
					map.setReadOnly(true);
				}
			}
			else {				
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.MAP_NEW);				
			}
			
			setMapAttributesIfNeeded(map);
		}
	}

	public void onRemove(MapModel map) {
		if (map instanceof MMapModel) {
			File f = map.getFile();
			if (f!=null) {
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.MAP_CLOSED, f);
				touchFileForAutoSaveBug(f);
			}
			
		}
	}

	private void touchFileForAutoSaveBug(File f) {
		try {
			FileUtils.touch(f);
		} catch (IOException e) {
			//LogUtils.warn(e);
		}		
	}

	public void onSavedAs(MapModel map) {
		if (map instanceof MMapModel) {
			File f = map.getFile();
			if (f!=null) {
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.MAP_SAVED, f);
			}
		}
	}

	public void onSaved(MapModel map) {
		if (map instanceof MMapModel) {
			File f = map.getFile();
			if (f!=null) {
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.MAP_SAVED, f);				
			}
		}
	}
		
	public void afterViewChange(Component oldView, Component newView) {
	}

	public void afterViewClose(Component oldView) {
	}

	public void afterViewCreated(Component mapView) {
		MapModel map = Controller.getCurrentController().getMapViewManager().getModel(mapView);
		setMapAttributesIfNeeded(map);		
	}

	public void beforeViewChange(Component oldView, Component newView) {
	}

	private void setMapAttributesIfNeeded(MapModel map) {
		if(map == null) {
			return;
		}
		
		DocearMapModelExtension dmme = map.getExtension(DocearMapModelExtension.class);		
		if (dmme == null) {
			DocearMapModelController.setModelWithCurrentVersion(map);			
		}
		else if (dmme.getMapId() == null || dmme.getMapId().trim().length()==0) {
			dmme.setMapId(DocearMapModelController.createMapId());
		}
		
		//DOCEAR: hack to prevent old trash maps from not having the type "trash"
		File f = map.getFile();
		File libraryPath = WorkspaceUtils.resolveURI(DocearController.getController().getLibraryPath());
		if (f != null) {
			if ("trash.mm".equals(f.getName())) {				
				if (f.getAbsolutePath().startsWith(libraryPath.getAbsolutePath())) {
					dmme.setType(DocearMapType.trash);
				}
			}
			else if ("temp.mm".equals(f.getName())) {				
				if (f.getAbsolutePath().startsWith(libraryPath.getAbsolutePath())) {
					dmme.setType(DocearMapType.temp);
				}
			}
		}
		
	}
	
	private void showMapIdConflictingDialogIfNeeded(MapModel map) {
		DocearMapModelExtension dmme = map.getExtension(DocearMapModelExtension.class);		
		if (dmme == null) {
			return;
		}
		
		File pathInXml = UrlManager.getController().absoluteFile(map, dmme.getUri());
		File physicalPath = map.getFile();
		
		if (pathInXml == null || physicalPath==null) {
			return;
		}
		
		if (!pathInXml.equals(physicalPath)) {
			if (pathInXml.exists()) {
				final MMapIO mapIO = (MMapIO) Controller.getCurrentModeController().getExtension(MapIO.class);				
				try {
					MMapModel otherMap = new MMapModel();
					mapIO.load(Compat.fileToUrl(pathInXml), otherMap);
					
					DocearMapModelExtension otherDmme = otherMap.getExtension(DocearMapModelExtension.class);
					if (dmme.getMapId().equals(otherDmme.getMapId())) {
						MapIdsConflictsPanel panel = new MapIdsConflictsPanel(physicalPath, pathInXml);
						JOptionPane.showMessageDialog(UITools.getFrame(), panel, TextUtils.getText("docear.conflicting_map_ids.title"), JOptionPane.QUESTION_MESSAGE);
						if (panel.isThisMapNew()) {							
							dmme.setMapId(DocearMapModelController.createMapId());
							map.setSaved(false);
							mapIO.save(map);
						}
						else if (panel.isOtherMapNew()) {							
							otherDmme.setMapId(DocearMapModelController.createMapId());
							otherMap.setSaved(false);
							mapIO.save(otherMap);							
						}
						else {
							map.setSaved(false);
							mapIO.save(map);
						}
					}
				} 
				catch (Exception e) {
					LogUtils.warn(e);
				}
			}
		}
	}
	


}
