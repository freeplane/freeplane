package org.freeplane.plugin.openmaps.mapelements;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

import org.freeplane.plugin.openmaps.LocationChoosenListener;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

/**
 * @author Blair Archibald
 */
public class OpenMapsController extends DefaultMapController implements MouseListener {

	private Set<LocationChoosenListener> Listeners;
	private int locationCount;
	
	public OpenMapsController(JMapViewer map) {
		super(map);
		configureButtons();
		
		Listeners = new HashSet<LocationChoosenListener>();
		locationCount = 0;
	}

	private void configureButtons() {
		this.setDoubleClickZoomEnabled(false);
		this.setMovementMouseButton(1);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		 if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			final Coordinate locationChoosen = getSelectedLocation(e.getPoint());
			if (locationCount < 1) {
				addMarkerToLocation(locationChoosen);
				locationCount++;
			}
			sendLocation(locationChoosen, getCurrentZoomLevel());	
		}
	}
	
	public void addLocationChoosenListener(LocationChoosenListener listener) {
		Listeners.add(listener);
	}
	
	public void removeLocationChoosenListener(LocationChoosenListener listener) {
		Listeners.remove(listener);
	}

	private void sendLocation(Coordinate locationChoosen, int zoom) {
		for (LocationChoosenListener l : Listeners) {
			l.locationChoosenAction(locationChoosen, zoom);
		}
	}

	public Coordinate getSelectedLocation(Point clickedLocation) {
		return map.getPosition(clickedLocation); 
	}
	
	public int getCurrentZoomLevel() {
		return map.getZoom();
	}

	private void addMarkerToLocation(final Coordinate locationChoosen) {
		map.addMapMarker(new MapMarkerDot(locationChoosen.getLat(), locationChoosen.getLon()));
	}

	public void zoomToLocation(Coordinate location, int zoom) {
		if(locationCount == 0) {
			addMarkerToLocation(location);
			locationCount++;
		} 
		map.setDisplayPositionByLatLon(new Point(map.getWidth() / 2, map.getHeight() / 2), location.getLat(), location.getLon(), zoom);
	}

}
