package org.freeplane.plugin.openmaps;

import org.freeplane.core.extension.IExtension;
import org.openstreetmap.gui.jmapviewer.Coordinate;

/**
 * @author Blair Archibald
 * Class stores the Point on the map to extend the node with. Simple Wrapper.
 */
public class OpenMapsExtension implements IExtension {
	private Coordinate location;

	public OpenMapsExtension() {
		location = new Coordinate(0,0);
	}
	
	public Coordinate getLocation() {
		return location;
	}

	public void updateLocation(double location_x, double location_y) {
		location = new Coordinate(location_x, location_y);
	}

	public void updateLocation(Coordinate locationChoosen) {
		location = new Coordinate(locationChoosen.getLat(), locationChoosen.getLon());
	}

}
