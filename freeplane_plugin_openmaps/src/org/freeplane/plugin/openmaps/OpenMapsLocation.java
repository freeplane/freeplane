package org.freeplane.plugin.openmaps;

public class OpenMapsLocation {
	public final float location_x;
	public final float location_y;

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