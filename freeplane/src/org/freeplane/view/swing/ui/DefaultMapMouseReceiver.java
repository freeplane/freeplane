/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.view.swing.ui;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.IMapMouseReceiver;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;

/**
 * @author Dimitry Polivaev
 * 06.01.2009
 */
public class DefaultMapMouseReceiver implements IMapMouseReceiver {
// // 	final private Controller controller;
	int originX = -1;
	int originY = -1;

	/**
	 *
	 */
	public DefaultMapMouseReceiver() {
		super();
//		this.controller = controller;
	}

	public void mouseDragged(final MouseEvent e) {
		final Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
	    final JComponent component = (JComponent) e.getComponent();
		final MapView mapView = getMapView(component);
		final boolean isEventPointVisible = component.getVisibleRect().contains(r);
		if (!isEventPointVisible) {
			component.scrollRectToVisible(r);
		}
		if (originX >= 0 && isEventPointVisible) {
			mapView.scrollBy(originX - e.getX(), originY - e.getY());
		}
	}

	private MapView getMapView(final Component component) {
	    if(component instanceof MapView)
	    	return (MapView) component;
	    return (MapView) SwingUtilities.getAncestorOfClass(MapView.class, component);
    }

	public void mousePressed(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			((MapView) Controller.getCurrentController().getViewController().getMapView()).setMoveCursor(true);
			originX = e.getX();
			originY = e.getY();
		}
	}

	public void mouseReleased(final MouseEvent e) {
		originX = -1;
		originY = -1;
		((MapView) Controller.getCurrentController().getViewController().getMapView()).setMoveCursor(false);
	}
}
