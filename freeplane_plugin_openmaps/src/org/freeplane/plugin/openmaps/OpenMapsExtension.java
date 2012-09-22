package org.freeplane.plugin.openmaps;

import org.freeplane.core.extension.IExtension;
import org.openstreetmap.gui.jmapviewer.Coordinate;

/**
 * @author Blair Archibald
 */
public class OpenMapsExtension implements IExtension {
	private Coordinate location;
	private int zoom;

	public OpenMapsExtension() {
		location = new Coordinate(0,0);
		zoom = 0;
	}
	
	public Coordinate getLocation() {
		return location;
	}
	
	public int getZoom() {
		return zoom;
	}
	
	public void updateZoom(int newZoom) {
		zoom = newZoom;
	}

	public void updateLocation(double location_x, double location_y) {
		location = new Coordinate(location_x, location_y);
	}

	public void updateLocation(Coordinate locationChoosen) {
		location = new Coordinate(locationChoosen.getLat(), locationChoosen.getLon());
	}

}
