/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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

import java.awt.Color;
import java.awt.Graphics2D;

import org.freeplane.features.nodestyle.ShapeConfigurationModel;

class BubbleMainView extends RectangleMainView {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void paintNodeShape(final Graphics2D g) {
		final int zoomedEdgeWidth = (int) getZoomedBorderWidth();
		g.drawRoundRect(zoomedEdgeWidth / 2, zoomedEdgeWidth / 2, getWidth() - zoomedEdgeWidth, getHeight() - zoomedEdgeWidth, 10, 10);
	}

	@Override
	protected void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
	}
    
    
    public BubbleMainView(ShapeConfigurationModel shapeConfiguration) {
		super(shapeConfiguration);
	}
}
