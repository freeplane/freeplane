package org.docear.plugin.core.listeners;

import java.io.File;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapModel;

public class MapLifeCycleListener implements IMapLifeCycleListener {

	@Override
	public void onCreate(MapModel map) {
		if (map instanceof MMapModel) {
			File f = map.getFile();
			if (f!=null) {
				DocearController.getController().getDocearEventLogger().write(this, DocearLogEvent.MAP_OPENED, f);
			}
			else {
				//if (map. == null) {
					DocearController.getController().getDocearEventLogger().write(this, DocearLogEvent.MAP_NEW);
				//}
			}
		}
	}

	@Override
	public void onRemove(MapModel map) {
		if (map instanceof MMapModel) {
			File f = map.getFile();
			if (f!=null) {
				DocearController.getController().getDocearEventLogger().write(this, DocearLogEvent.MAP_CLOSED, f);
			}
		}
	}

	@Override
	public void onSavedAs(MapModel map) {
		if (map instanceof MMapModel) {
			File f = map.getFile();
			if (f!=null) {
				DocearController.getController().getDocearEventLogger().write(this, DocearLogEvent.MAP_SAVED, f);
			}
		}
	}

	@Override
	public void onSaved(MapModel map) {
		if (map instanceof MMapModel) {
			File f = map.getFile();
			if (f!=null) {
				DocearController.getController().getDocearEventLogger().write(this, DocearLogEvent.MAP_SAVED, f);
			}
		}
	}

}
