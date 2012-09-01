package org.freeplane.plugin.openmaps;

import org.freeplane.core.extension.IExtension;

/**
 * @author Blair Archibald
 * Class stores the Point on the map to extend the node with. Simple Wrapper.
 */
public class OpenMapsExtension implements IExtension {
	private OpenMapsLocation location;

	public OpenMapsExtension() {
		location = new OpenMapsLocation(0,0);
	}
	
	public OpenMapsLocation getLocation() {
		return location;
	}

	public void updateLocation(float location_x, float location_y) {
		location = new OpenMapsLocation(location_x, location_y);
	}

	public void updateLocation(OpenMapsLocation newLocation) {
		location = newLocation;
	}

}
