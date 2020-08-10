package org.freeplane.plugin.openmaps.mapelements;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedHashSet;
import java.util.Set;

import org.freeplane.plugin.openmaps.LocationChoosenListener;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

/**
 * @author Blair Archibald
 */
public class OpenMapsController extends DefaultMapController implements MouseListener {

	private Set<LocationChoosenListener> Listeners;
	private MapMarker currentLocation;
	
	public OpenMapsController(JMapViewer map) {
		super(map);
		configureButtons();
		
		Listeners = new LinkedHashSet<LocationChoosenListener>();
		currentLocation = null;
	}

	private void configureButtons() {
		this.setDoubleClickZoomEnabled(false);
		this.setMovementMouseButton(1);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		 if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			final ICoordinate locationChoosen = getSelectedLocation(e.getPoint());
			setMarkerAtLocation(locationChoosen);
			sendLocation(locationChoosen, getCurrentZoomLevel());	
		}
	}
	
	public void addLocationChoosenListener(LocationChoosenListener listener) {
		Listeners.add(listener);
	}
	
	public void removeLocationChoosenListener(LocationChoosenListener listener) {
		Listeners.remove(listener);
	}

	private void sendLocation(ICoordinate locationChoosen, int zoom) {
		for (LocationChoosenListener l : Listeners) {
			l.locationChoosenAction(locationChoosen, zoom);
		}
	}

	public ICoordinate getSelectedLocation(Point clickedLocation) {
		return map.getPosition(clickedLocation); 
	}
	
	public int getCurrentZoomLevel() {
		return map.getZoom();
	}

	private void setMarkerAtLocation(final ICoordinate locationChoosen) {
		if (currentLocation != null)
			map.removeMapMarker(currentLocation);
		map.addMapMarker(currentLocation = new MapMarkerDot(locationChoosen.getLat(), locationChoosen.getLon()));
	}
	
	public void zoomToLocation(Coordinate location, int zoom) {
		setMarkerAtLocation(location);

		final OsmMercator osmMercator = new OsmMercator();
		
        int x = (int)osmMercator.lonToX(location.getLon(), zoom);
        int y = (int)osmMercator.latToY(location.getLat(), zoom);
		map.setDisplayPosition(new Point(map.getWidth() / 2, map.getHeight() / 2), x, y, zoom);
	}

}
