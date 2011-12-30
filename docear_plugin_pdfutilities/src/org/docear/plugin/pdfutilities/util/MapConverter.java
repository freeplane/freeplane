package org.docear.plugin.pdfutilities.util;

import java.util.HashSet;
import java.util.List;

import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.listeners.ISplmmMapsConvertListener;
import org.docear.plugin.core.listeners.SplmmMapsConvertEvent;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.docear.plugin.pdfutilities.features.AnnotationModelUpdater;
import org.docear.plugin.pdfutilities.features.MonitorungNodeUpdater;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;

public class MapConverter {
	
	private final static HashSet<ISplmmMapsConvertListener> mapsConvertedListener = new HashSet<ISplmmMapsConvertListener>();
	
	public static void addMapsConvertedListener(ISplmmMapsConvertListener listener) {
		mapsConvertedListener.add(listener);
	}
	
	public static void removeMapsConvertedListener(ISplmmMapsConvertListener listener) {
		mapsConvertedListener.remove(listener);
	}
	
	public static void fireConvertMapsEvent(MindmapUpdateController mindmapUpdateController) {
		for (ISplmmMapsConvertListener listener : mapsConvertedListener) {
			SplmmMapsConvertEvent event = new SplmmMapsConvertEvent(mindmapUpdateController);
			listener.mapsConvert(event);
		}
		
	}
	
	public static boolean convert(final List<MapModel> maps){
		if(maps == null || maps.size() <= 0) return false;

		MindmapUpdateController mindmapUpdateController = new MindmapUpdateController();
		mindmapUpdateController.addMindmapUpdater(new AnnotationModelUpdater("Converting Mindmaps...."));
		mindmapUpdateController.addMindmapUpdater(new MonitorungNodeUpdater("Updating Monitoring folder...."));
		fireConvertMapsEvent(mindmapUpdateController);
		//mindmapUpdateController.addMindmapUpdater(new MindmapLinkTypeUpdater("Converting hyperlinks...."));
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
