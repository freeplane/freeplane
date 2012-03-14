package org.docear.plugin.backup.listeners;

import java.io.File;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapModel;

public class MapLifeCycleListener implements IMapLifeCycleListener {
	
	public void onCreate(MapModel map) {
		File f = map.getFile();
		if (map instanceof MMapModel && f==null) {
			if (f==null) {			
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.MAP_NEW);			
			
			}
		}
	}

	public void onRemove(MapModel map) {
	}

	public void onSavedAs(MapModel map) {
	}

	public void onSaved(MapModel map) {
	}

}
