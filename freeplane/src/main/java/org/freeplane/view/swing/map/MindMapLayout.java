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
package org.freeplane.view.swing.map;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

/**
 * This class will Layout the Nodes and Edges of an MapView.
 */
public class MindMapLayout implements LayoutManager {
	final static int BORDER = 30;
	public final static int MINIMAL_LEAF_WIDTH = 150;

	public MindMapLayout() {
	}

	public void addLayoutComponent(final String name, final Component comp) {
	}

	private int calcXBorderSize(final MapView map) {
		int xBorderSize;
		final Dimension visibleSize = map.getViewportSize();
		final int minBorderWidth = map.getZoomed(MindMapLayout.BORDER + MindMapLayout.MINIMAL_LEAF_WIDTH);
		if (visibleSize != null) {
			xBorderSize = Math.max(visibleSize.width, minBorderWidth);
		}
		else {
			xBorderSize = minBorderWidth;
		}
		return xBorderSize;
	}

	/**
	 * @param map
	 */
	private int calcYBorderSize(final MapView map) {
		int yBorderSize;
		final int minBorderHeight = map.getZoomed(MindMapLayout.BORDER);
		final Dimension visibleSize = map.getViewportSize();
		if (visibleSize != null) {
			yBorderSize = Math.max(visibleSize.height, minBorderHeight);
		}
		else {
			yBorderSize = minBorderHeight;
		}
		return yBorderSize;
	}

	private NodeView getRoot(final Container c) {
		return ((MapView) c).getRoot();
	}

	public void layoutContainer(final Container c) {
		final MapView mapView = (MapView) c;
		final int calcXBorderSize = calcXBorderSize(mapView);
		final int calcYBorderSize = calcYBorderSize(mapView);
		getRoot(mapView).validate();
		getRoot(mapView).setLocation(calcXBorderSize, calcYBorderSize);
	}

	public Dimension minimumLayoutSize(final Container parent) {
		return new Dimension(200, 200);
	}

	public Dimension preferredLayoutSize(final Container c) {
		final MapView mapView = (MapView) c;
		final Dimension preferredSize = mapView.getRoot().getPreferredSize();
		return new Dimension(2 * calcXBorderSize(mapView) + preferredSize.width, 2 * calcYBorderSize(mapView)
		        + preferredSize.height);
	}

	public void removeLayoutComponent(final Component comp) {
	}
}
