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
import java.awt.Point;

import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodestyle.NodeGeometryModel;

class OvalPainter extends VariableInsetsPainter {
	private static final double MARGIN_FACTOR = Math.sqrt(2);
	OvalPainter(MainView mainView,  NodeGeometryModel shapeConfigurationModel) {
        super(mainView, shapeConfigurationModel);
    }

	@Override
	double getVerticalMarginFactor() {
		return MARGIN_FACTOR;
	}

	@Override
	double getHorizontalMarginFactor() {
		return MARGIN_FACTOR;
	}

	@Override
	void paintNodeShape(final Graphics2D g) {
		final int zoomedEdgeWidth = (int) mainView.getPaintedBorderWidth();
		g.drawOval(zoomedEdgeWidth / 2, zoomedEdgeWidth / 2, mainView.getWidth() - zoomedEdgeWidth, mainView.getHeight() - zoomedEdgeWidth);
	}

	@Override
	void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fillOval(1, 1, mainView.getWidth() - 2, mainView.getHeight() - 2);
	}

	@Override
	Point getConnectorPoint(Point p) {
		return getShapeConfiguration().isUniform() || !MainView.USE_COMMON_OUT_POINT_FOR_ROOT_NODE && mainView.getNodeView().isRoot() ? getConnectorPointAtTheOvalBorder(p) : super.getConnectorPoint(p);
	}



	@Override
	Dimension getPreferredSize() {
		if (mainView.isPreferredSizeSet()) {
			return super.getPreferredSize();
		}
		if(getShapeConfiguration().isUniform()){
			final Dimension prefSize = getPreferredRectangleSizeWithoutMargin(mainView.getMaximumWidth());
			double w = prefSize.width + getMinimumHorizontalInset();
			double h = prefSize.height + getMinimumVerticalInset();
			int diameter = (int)(Math.ceil(Math.sqrt(w * w + h * h)));
			prefSize.width = prefSize.height = mainView.limitWidth(diameter);
			return prefSize;
		}
		else
			return super.getPreferredSize();
	}

	Point getConnectorPointAtTheOvalBorder(Point p) {
		final double nWidth = mainView.getWidth() / 2f;
    	final double nHeight = mainView.getHeight() / 2f;
    	int dx = Math.max(Math.abs(p.x -  mainView.getWidth()/2), mainView.getNodeView().getZoomed(LocationModel.DEFAULT_HGAP_PX));
    	if(p.x < mainView.getWidth()/2)
    		dx = -dx;
    	double angle = Math.atan((p.y - nHeight) / dx);
    	if (dx < 0) {
    		angle += Math.PI;
    	}
    	final Point out = new Point((int) ((1f + Math.cos(angle)) * nWidth), (int) ((1f + Math.sin(angle)) * nHeight));
		return out;
	}

}
