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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;

import javax.swing.SwingConstants;

import org.freeplane.features.nodestyle.ShapeConfigurationModel;
import org.freeplane.features.nodestyle.NodeStyleModel.Shape;

class NarrowHexagonMainView extends VariableInsetsMainView {
	private static final double VERTICAL_MARGIN_FACTOR = (Math.sqrt(3) + 1)/2;
	private static final double UNIFORM_HEIGHT_TO_WIDTH_RELATION = 2 / Math.sqrt(3);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NarrowHexagonMainView(ShapeConfigurationModel shapeConfigurationModel) {
		super(shapeConfigurationModel);
	}

	protected double getVerticalMarginFactor() {
		return VERTICAL_MARGIN_FACTOR;
	}

	protected double getHorizontalMarginFactor() {
		return 1;
	}
	
	@Override
	public Dimension getPreferredSize() {
		if (isPreferredSizeSet()) {
			return super.getPreferredSize();
		}
		if(getShapeConfiguration().isUniform()){
			final Dimension prefSize = getPreferredSizeWithoutMargin(getMaximumWidth());
			int w = prefSize.width;
			int h = prefSize.height;
			int diameter = (int)(Math.ceil(Math.sqrt(w * w + h * h))) ;
			prefSize.width = (int) Math.ceil(Math.max(diameter, prefSize.width + getZoom() * getMinimumHorizontalInset()));
			prefSize.height = (int) Math.ceil(Math.max(diameter, prefSize.height + getZoom() * getMinimumVerticalInset()));
			if(prefSize.width < getMinimumWidth())
				prefSize.width = getMinimumWidth();
			if (prefSize.height < prefSize.width * UNIFORM_HEIGHT_TO_WIDTH_RELATION)
				prefSize.height = (int) (prefSize.width * UNIFORM_HEIGHT_TO_WIDTH_RELATION);
			else
				prefSize.width = (int) (prefSize.height / UNIFORM_HEIGHT_TO_WIDTH_RELATION);
			return prefSize;
		}
		else
			return super.getPreferredSize();
	}

	
	@Override
	protected void paintNodeShape(final Graphics2D g) {
		Polygon polygon = getPaintedShape();
		g.draw(polygon);
	}

	protected Polygon getPaintedShape() {
		final Polygon polygon;
		if(getShapeConfiguration().isUniform()){
			int[] xCoords = new int[]{getWidth() / 2, 0,  0,  getWidth() / 2, getWidth() - 1, getWidth() - 1};
			int[] yCoords = new int[]{0,   getHeight()/4, 3 * getHeight() /4 , getHeight(),      3 * getHeight() / 4, getHeight() / 4};
			polygon = new Polygon(xCoords, yCoords, xCoords.length);
		}
		else {
			final int zoomedVerticalInset = (int) (getHeight() * (1 - 1 / getVerticalMarginFactor() ) / 2);
			int[] xCoords = new int[]{0,                        getWidth()/2, getWidth() -1,            getWidth() - 1,                             getWidth()/2,   0};
			int[] yCoords = new int[]{zoomedVerticalInset, 0,            zoomedVerticalInset, getHeight() - zoomedVerticalInset - 1, getHeight() - 1,getHeight() - zoomedVerticalInset - 1, };
			polygon = new Polygon(xCoords, yCoords, xCoords.length);
		}
		return polygon;
	}

	@Override
	protected void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fill(getPaintedShape());
	}
}
