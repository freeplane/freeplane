package org.freeplane.plugin.openmaps.mapelements;

import org.freeplane.plugin.openmaps.OpenMapsLocation;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

public class OpenMapsController {
	
	private final JMapViewer map;
	
	public OpenMapsController(JMapViewer map){
		this.map = map;
	}
	
	public OpenMapsLocation getSelectedLocation() {
		return new OpenMapsLocation (100,100);
		//return null; //Causes issues obviously
	}

}
