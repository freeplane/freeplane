package org.freeplane.plugin.openmaps.mapelements;

import org.freeplane.plugin.openmaps.OpenMapsLocation;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

public class OpenMapsController {
	
	private final JMapViewer map;
	
	public OpenMapsController(JMapViewer map){
		this.map = map;
	}
	
	public OpenMapsLocation getSelectedLocation() {
		//FIXME remove stub
		map.addMapMarker(new MapMarkerDot(10.0 ,50.0));
		map.repaint();
		return new OpenMapsLocation (10.0f,50.0f);
		//return null; //Causes issues obviously
	}

}
