package org.freeplane.features.presentations.mindmapmode;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.MapExtensions;

public class MapPresentations implements IExtension{
	static {
		MapExtensions.registerMapExtension(MapPresentations.class);
	}
	
	public final NamedElementCollection<Presentation> presentations = new NamedElementCollection<>(Presentation.class);
	
	static public MapPresentations getPresentations(MapModel map) {
		final NodeModel rootNode = map.getRootNode();
		MapPresentations mapPresentations = rootNode.getExtension(MapPresentations.class);
		if(mapPresentations == null) {
			mapPresentations = new MapPresentations();
			rootNode.addExtension(mapPresentations);
		}
		return mapPresentations;
	}
}
