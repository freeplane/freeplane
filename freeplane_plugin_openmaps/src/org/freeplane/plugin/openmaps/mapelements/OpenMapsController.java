package org.freeplane.plugin.openmaps.mapelements;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.freeplane.plugin.openmaps.OpenMapsLocation;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

public class OpenMapsController extends DefaultMapController implements MouseListener {

	public OpenMapsController(JMapViewer map) {
		super(map);
		this.setDoubleClickZoomEnabled(false);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		//FIXME this makes no sense?
		 if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			final Coordinate locationChoosen = getSelectedLocation(e.getPoint());
		}
	}

	public Coordinate getSelectedLocation(Point clickedLocation) {
		map.addMapMarker(new MapMarkerDot(clickedLocation.x, clickedLocation.y));
		map.repaint();
		return map.getPosition(clickedLocation);
	}

}
