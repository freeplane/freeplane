/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General License for more details.
 *
 *  You should have received a copy of the GNU General License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.view.swing.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;

import org.freeplane.features.nodestyle.NodeGeometryModel;

class WideHexagonPainter extends VariableInsetsPainter {
	private static final double VERTICAL_MARGIN_FACTOR = Math.sqrt(2);
	private static final double UNIFORM_HEIGHT_TO_WIDTH_RELATION = Math.sqrt(3)/2;
	private static final double HORIZONTAL_MARGIN_FACTOR = Math.sqrt(2)/ UNIFORM_HEIGHT_TO_WIDTH_RELATION;
	WideHexagonPainter(MainView mainView, NodeGeometryModel shapeConfigurationModel) {
		super(mainView, shapeConfigurationModel);
	}

	@Override
	double getVerticalMarginFactor() {
		return VERTICAL_MARGIN_FACTOR;
	}

	@Override
	double getHorizontalMarginFactor() {
		return HORIZONTAL_MARGIN_FACTOR;
	}

	@Override
	Dimension getPreferredSize() {
		if (mainView.isPreferredSizeSet()) {
			return super.getPreferredSize();
		}
		if(getShapeConfiguration().isUniform()) {
			final Dimension prefSize = getPreferredRectangleSizeWithoutMargin(mainView.getMaximumWidth());
			double w = prefSize.width + getMinimumHorizontalInset();
			double h = prefSize.height + getMinimumVerticalInset();
			double diameter = Math.sqrt(w * w + h * h);
			double width = mainView.limitWidth (diameter/ UNIFORM_HEIGHT_TO_WIDTH_RELATION);
			prefSize.width = (int) Math.ceil(width);
			prefSize.height = (int) (width * UNIFORM_HEIGHT_TO_WIDTH_RELATION);
			return prefSize;
		}
		else
			return super.getPreferredSize();
	}


	@Override
	void paintNodeShape(final Graphics2D g) {
		Polygon polygon = getPaintedShape();
		g.draw(polygon);
	}

	Polygon getPaintedShape() {
		double[] xCoords;
		double[] yCoords;
		if(getShapeConfiguration().isUniform()){
			xCoords = new double[]{0, 1/4f, 3/4f , 1, 3/4f, 1/4f};
			yCoords = new double[]{1/2f, 0,  0,  1/2f, 1, 1};
		}
		else {
			final double zoomedHorizontalInset = (1 - 1 / getHorizontalMarginFactor() ) / 2;
			xCoords = new double[]{0, zoomedHorizontalInset, 1-zoomedHorizontalInset, 1, 1-zoomedHorizontalInset, zoomedHorizontalInset};
			yCoords = new double[]{1/2f, 0, 0, 1/2f, 1, 1};
		}
		return polygonOf(xCoords, yCoords);
	}

	@Override
	void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fill(getPaintedShape());
	}
}
