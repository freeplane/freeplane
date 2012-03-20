package org.docear.plugin.core.listeners;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.features.DocearMapModelExtension;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewChangeListener;

public class MapLifeCycleAndViewListener implements IMapLifeCycleListener, IMapViewChangeListener {

	public void onCreate(MapModel map) {
		if (map instanceof MMapModel) {
			File f = map.getFile();
			if (f!=null) {
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.MAP_OPENED, f);
			}
			else {
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.MAP_NEW);				
			}
			
			setMapIdIfNeeded(map);
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
			LogUtils.warn(e);
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
		setMapIdIfNeeded(map);
	}

	public void beforeViewChange(Component oldView, Component newView) {
	}

	private void setMapIdIfNeeded(MapModel map) {
		if(map == null) {
			return;
		}
		
		DocearMapModelExtension dmme = map.getExtension(DocearMapModelExtension.class);		
		if (dmme == null || dmme.getMapId() == null || dmme.getMapId().trim().length()==0) {
			DocearMapModelController.setModelWithCurrentVersion(map);
			dmme = map.getExtension(DocearMapModelExtension.class);
		}
	}


}
