package org.freeplane.plugin.openmaps.mapElements;

import org.freeplane.plugin.openmaps.OpenMapsLocation;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

public class MapController {
	
	private final JMapViewer map;
	
	public MapController(JMapViewer map){
		this.map = map;
	}
	
	public OpenMapsLocation getSelectedLocation() {
		return new OpenMapsLocation (100,100);
		//return null; //Causes issues obviously
	}

}
