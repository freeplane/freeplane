package org.freeplane.plugin.openmaps;

import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.JMapViewer;

public class MapController {
	
	private final JMapViewer map;
	
	public MapController(JMapViewer map){
		this.map = map;
	}
	
	public Point getSelectedLocation() {
		return new Point (100,100);
	}

}
