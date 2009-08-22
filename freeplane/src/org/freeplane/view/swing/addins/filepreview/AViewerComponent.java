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
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.freeplane.view.swing.map.MapView;

public abstract class AViewerComponent extends JComponent {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	protected float zoom = 1f;

	public AViewerComponent() {
		super();
	}
	
	abstract protected Dimension getOriginalSize();

	@Override
    public Dimension getPreferredSize() {
    	Dimension preferredSize = super.getPreferredSize();
    	MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, this);
    	float newZoom = mapView.getZoom();
    	if(zoom != newZoom){
    		float ratio = newZoom/ zoom;
    		preferredSize.width = (int)(Math.rint(preferredSize.width * ratio));
    		preferredSize.height = (int)(Math.rint(preferredSize.height * ratio));
    		setPreferredSize(preferredSize);
    		zoom = newZoom;
    	}
    	return preferredSize;
    }
}
