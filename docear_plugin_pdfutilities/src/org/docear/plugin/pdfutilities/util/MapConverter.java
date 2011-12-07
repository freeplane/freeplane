package org.docear.plugin.pdfutilities.util;

import java.util.List;

import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.docear.plugin.pdfutilities.features.AnnotationModelUpdater;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;

public class MapConverter {
	
	public static boolean convert(final List<MapModel> maps){
		if(maps == null || maps.size() <= 0) return false;
		MindmapUpdateController mindmapUpdateController = new MindmapUpdateController();
		mindmapUpdateController.addMindmapUpdater(new AnnotationModelUpdater("Converting Mindmaps...."));
		if(mindmapUpdateController.updateMindmapsInList(maps)){
			for(MapModel map : maps){				
				DocearMapModelController.setModelWithCurrentVersion(map);				
				map.setSaved(false);
				map.setReadOnly(false);
				((MFileManager) UrlManager.getController()).save(map, false);
			}			
			return true;
		}		
		return false;
	}
}
