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

public class OpenMapsController extends DefaultMapController implements MouseListener {

	private Set<LocationChoosenListener> Listeners;
	
	public OpenMapsController(JMapViewer map) {
		super(map);
		Listeners = new HashSet<LocationChoosenListener>();
		this.setDoubleClickZoomEnabled(false);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		 if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			final Coordinate locationChoosen = getSelectedLocation(e.getPoint());
			sendLocation(locationChoosen);	
		}
	}
	
	public void addLocationChoosenListener(LocationChoosenListener listener) {
		Listeners.add(listener);
	}

	private void sendLocation(Coordinate locationChoosen) {
		for (LocationChoosenListener l : Listeners) {
			l.locationChoosenAction(locationChoosen);
		}
		
	}

	public Coordinate getSelectedLocation(Point clickedLocation) {
		final Coordinate locationChoosen = map.getPosition(clickedLocation); 
		map.addMapMarker(new MapMarkerDot(locationChoosen.getLat(), locationChoosen.getLon()));
		return locationChoosen;
	}

}
