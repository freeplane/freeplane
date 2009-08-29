/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.view.swing.addins.filepreview;

/**
 * @author Dimitry Polivaev
 * 22.08.2009
 */
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.freeplane.view.swing.map.MapView;

public class ViewerLayoutManager implements LayoutManager {
	private float zoom;

	/**
     * 
     */
	public ViewerLayoutManager(float zoom) {
		super();
		this.zoom = zoom;
	}

	public void addLayoutComponent(String name, Component comp) {
    }

	public void layoutContainer(final Container parent) {
		if(! parent.isPreferredSizeSet()){
			throw new IllegalStateException("preferred size not set for " + parent);
		}
    	Dimension preferredSize = parent.getPreferredSize();
    	MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, parent);
    	if(mapView == null){
    		return;
    	}
    	float newZoom = mapView.getZoom();
    	if(zoom != newZoom){
    		float ratio = newZoom/ zoom;
    		preferredSize.width = (int)(Math.rint(preferredSize.width * ratio));
    		preferredSize.height = (int)(Math.rint(preferredSize.height * ratio));
    		parent.setPreferredSize(preferredSize);
    		zoom = newZoom;
    	}
    }

	public Dimension minimumLayoutSize(Container parent) {
	    return new Dimension(0, 0);
    }

	public Dimension preferredLayoutSize(Container parent) {
	    return parent.getPreferredSize();
    }

	public void removeLayoutComponent(Component comp) {
    }
}
