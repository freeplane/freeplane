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
import java.awt.Point;

import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodestyle.ShapeConfigurationModel;

class OvalMainView extends VariableInsetsMainView {
	private static final double MARGIN_FACTOR = Math.sqrt(2);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public OvalMainView(ShapeConfigurationModel shapeConfigurationModel) {
        super(shapeConfigurationModel);
    }
	
	protected double getVerticalMarginFactor() {
		return MARGIN_FACTOR;
	}

	protected double getHorizontalMarginFactor() {
		return MARGIN_FACTOR;
	}

	@Override
	protected void paintNodeShape(final Graphics2D g) {
		final int zoomedEdgeWidth = (int) getZoomedBorderWidth();
		g.drawOval(zoomedEdgeWidth / 2, zoomedEdgeWidth / 2, getWidth() - zoomedEdgeWidth, getHeight() - zoomedEdgeWidth);
	}

	@Override
	protected void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fillOval(1, 1, getWidth() - 2, getHeight() - 2);
	}

	@Override
	public Point getConnectorPoint(Point p) {
		return getShapeConfiguration().isUniform() || !USE_COMMON_OUT_POINT_FOR_ROOT_NODE && getNodeView().isRoot() ? getConnectorPointAtTheOvalBorder(p) : super.getConnectorPoint(p);
	}
	
	
	
	@Override
	public Dimension getPreferredSize() {
		if (isPreferredSizeSet()) {
			return super.getPreferredSize();
		}
		if(getShapeConfiguration().isUniform()){
			final Dimension prefSize = getPreferredRectangleSizeWithoutMargin(getMaximumWidth());
			double w = prefSize.width + getMinimumHorizontalInset();
			double h = prefSize.height + getMinimumVerticalInset();
			int diameter = (int)(Math.ceil(Math.sqrt(w * w + h * h)));
			prefSize.width = prefSize.height = limitWidth(diameter);
			return prefSize;
		}
		else 
			return super.getPreferredSize();
	}

	protected Point getConnectorPointAtTheOvalBorder(Point p) {
		final double nWidth = this.getWidth() / 2f;
    	final double nHeight = this.getHeight() / 2f;
    	int dx = Math.max(Math.abs(p.x -  this.getWidth()/2), getNodeView().getZoomed(LocationModel.DEFAULT_HGAP_PX));
    	if(p.x < this.getWidth()/2)
    		dx = -dx;
    	double angle = Math.atan((p.y - nHeight) / dx);
    	if (dx < 0) {
    		angle += Math.PI;
    	}
    	final Point out = new Point((int) ((1f + Math.cos(angle)) * nWidth), (int) ((1f + Math.sin(angle)) * nHeight));
		return out;
	}

}
