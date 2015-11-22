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

import javax.swing.SwingConstants;

import org.freeplane.features.nodestyle.NodeStyleModel.Shape;

class NarrowHexagonMainView extends VariableInsetsMainView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public NarrowHexagonMainView() {
        super();
        setHorizontalAlignment(SwingConstants.CENTER);
    }

	protected double getVerticalMarginFactor() {
		return 2;
	}

	protected double getHorizontalMarginFactor() {
		return 1;
	}
	
	protected int getMinimumHorizontalInset(){
		return 3;
	}


	@Override
    public
    Shape getShape() {
		return Shape.narrow_hexagon;
	}

	@Override
	protected void paintNodeShape(final Graphics2D g) {
		Polygon polygon = getPaintedShape();
		g.draw(polygon);
	}

	protected Polygon getPaintedShape() {
		final int zoomedVerticalInset = (int) (getHeight() * (1 - 1 / getVerticalMarginFactor() ) / 2);
		int[] xCoords = new int[]{0,                        getWidth()/2, getWidth() -1,            getWidth() - 1,                             getWidth()/2,   0};
		int[] yCoords = new int[]{zoomedVerticalInset, 0,            zoomedVerticalInset, getHeight() - zoomedVerticalInset - 1, getHeight() - 1,getHeight() - zoomedVerticalInset - 1, };
		Polygon polygon = new Polygon(xCoords, yCoords, xCoords.length);
		return polygon;
	}

	@Override
	protected void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fill(getPaintedShape());
	}
}
