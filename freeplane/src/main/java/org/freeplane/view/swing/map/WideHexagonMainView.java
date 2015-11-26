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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import org.freeplane.features.nodestyle.NodeStyleModel.Shape;

class WideHexagonMainView extends VariableInsetsMainView {
	private static final double HORIZONTAL_MARGIN_FACTOR = (Math.sqrt(3) + 1)/2;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public WideHexagonMainView() {
        super();
    }

	protected double getVerticalMarginFactor() {
		return 1.0;
	}
	
	protected int getMinimumVerticalInset(){
		return 3;
	}

	protected double getHorizontalMarginFactor() {
		return HORIZONTAL_MARGIN_FACTOR;
	}
	
	@Override
    public
    Shape getShape() {
		return Shape.wide_hexagon;
	}

	@Override
	protected void paintNodeShape(final Graphics2D g) {
		Polygon polygon = getPaintedShape();
		g.draw(polygon);
	}

	protected Polygon getPaintedShape() {
		final int zoomedHorizontalInset = (int) (getWidth() * (1 - 1 / getHorizontalMarginFactor()) / 2);
		int[] xCoords = new int[]{0,               zoomedHorizontalInset, getWidth() - zoomedHorizontalInset - 1, getWidth(),      getWidth() - zoomedHorizontalInset - 1, zoomedHorizontalInset};
		int[] yCoords = new int[]{getHeight() / 2, 0,                     0,                                      getHeight() / 2, getHeight() - 1,                        getHeight() - 1};
		Polygon polygon = new Polygon(xCoords, yCoords, xCoords.length);
		return polygon;
	}

	@Override
	protected void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fill(getPaintedShape());
	}
}
