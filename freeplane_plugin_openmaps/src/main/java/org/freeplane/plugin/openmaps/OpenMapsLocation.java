package org.freeplane.plugin.openmaps;

/**
 * @author Blair Archibald
 */
public class OpenMapsLocation {
	private final float location_x;
	private final float location_y;

	public OpenMapsLocation(float x, float y) {
		location_x = x;
		location_y = y;
	}
	
	public float getXLocation() {
		return location_x;
	}
	
	public float getYLocation() {
		return location_y;
	}
}
