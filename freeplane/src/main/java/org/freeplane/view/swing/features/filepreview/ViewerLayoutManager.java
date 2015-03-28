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
package org.freeplane.view.swing.features.filepreview;

/**
 * @author Dimitry Polivaev
 * 22.08.2009
 */
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.SwingUtilities;

import org.freeplane.view.swing.map.MapView;

public class ViewerLayoutManager implements LayoutManager {
	private float zoom;
	private ExternalResource externalResource;
	private Dimension originalSize;

	/**
	 * @param externalResource TODO
	 * @param originalSize TODO
	 * 
	 */
	public ViewerLayoutManager(final float zoom, ExternalResource externalResource, Dimension originalSize) {
		super();
		this.zoom = zoom;
		this.externalResource = externalResource;
		this.originalSize = originalSize;
	}

	public void addLayoutComponent(final String name, final Component comp) {
	}

	public void layoutContainer(final Container parent) {
		if (!parent.isPreferredSizeSet()) {
			throw new IllegalStateException("preferred size not set for " + parent);
		}
		final MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, parent);
		if (mapView == null) {
			return;
		}
		final float newZoom = mapView.getZoom();
		if (zoom != newZoom) {
			zoom = newZoom;
			final Dimension preferredSize = calculatePreferredSize();
			parent.setPreferredSize(preferredSize);
		}
	}

	public Dimension calculatePreferredSize() {
		int width = (int) (Math.ceil(originalSize.width * externalResource.getZoom() * zoom));
		int height = (int) (Math.ceil(originalSize.height * externalResource.getZoom() * zoom));
		final Dimension preferredSize = new Dimension(width, height);
		return preferredSize;
	}

	public Dimension minimumLayoutSize(final Container parent) {
		return new Dimension(0, 0);
	}

	public Dimension preferredLayoutSize(final Container parent) {
		return parent.getPreferredSize();
	}

	public void removeLayoutComponent(final Component comp) {
	}
}
