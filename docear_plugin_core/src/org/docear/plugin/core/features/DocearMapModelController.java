package org.docear.plugin.core.features;

import org.docear.plugin.core.util.CoreUtils;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class DocearMapModelController implements IExtension{
	
	public static DocearMapModelController getController() {
		return getController(Controller.getCurrentModeController());
	}

	public static DocearMapModelController getController(ModeController modeController) {
		return (DocearMapModelController) modeController.getExtension(DocearMapModelController.class);
	}
	public static void install( final DocearMapModelController docearMapModelController) {
		Controller.getCurrentModeController().addExtension(DocearMapModelController.class, docearMapModelController);
	}
	
	public DocearMapModelController(final ModeController modeController){
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		DocearMapModelExtensionXmlBuilder builder = new DocearMapModelExtensionXmlBuilder();
		builder.registerBy(readManager, writeManager);
//		Controller.getCurrentController().getMapViewManager().addMapSelectionListener(new IMapSelectionListener() {
//			
//			public void beforeMapChange(MapModel oldMap, MapModel newMap) {}
//			
//			public void afterMapClose(MapModel oldMap) {}
//			
//			public void afterMapChange(MapModel oldMap, MapModel newMap) {
//				if(newMap == null || newMap.getExtension(DocearMapModelExtension.class) != null || newMap.getFile() != null) return;
//				DocearMapModelController.setModelWithCurrentVersion(newMap);					
//			}
//		});
	}
	
	public static DocearMapModelExtension getModel(final MapModel map) {
		DocearMapModelExtension mapModelExtension = (DocearMapModelExtension) map.getExtension(DocearMapModelExtension.class);
		
		return mapModelExtension;
	}
	
	public static void setModel(final MapModel map, final DocearMapModelExtension mapModelExtension) {
		final DocearMapModelExtension oldMapModelExtension = (DocearMapModelExtension) map.getExtension(DocearMapModelExtension.class);
		if (mapModelExtension != null && oldMapModelExtension == null) {
			map.addExtension(mapModelExtension);
		}
		else if (mapModelExtension == null && oldMapModelExtension != null) {
			map.removeExtension(DocearMapModelExtension.class);
		}
		else if (mapModelExtension != null && oldMapModelExtension != null) {
			map.removeExtension(DocearMapModelExtension.class);
			map.addExtension(mapModelExtension);
		}
	}
	
	public static void setModelWithCurrentVersion(final MapModel map) {
		DocearMapModelExtension mapModelExtension = new DocearMapModelExtension();
		ResourceController resourceController = ResourceController.getResourceController();
		mapModelExtension.setVersion(resourceController.getProperty("docear_map_extension_version"));
		mapModelExtension.setMapId(createMapId());
		DocearMapModelController.setModel(map, mapModelExtension);
	}
	
	public static String createMapId() {
		return ""+System.currentTimeMillis()+"_"+CoreUtils.createRandomString(130);
	}

}
