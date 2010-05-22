/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.view.swing.map.link;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;

import org.freeplane.features.common.link.ConnectorModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 * Apr 29, 2010
 */
abstract class AConnectorView  implements ILinkView {
	protected final ConnectorModel connectorModel;
	protected final NodeView source;
	protected final NodeView target;

	protected double getZoom() {
		return getMap().getZoom();
	}
	protected MapView getMap() {
		return (source == null) ? target.getMap() : source.getMap();
	}

	/**
     * @param p1
     *            is the start point
     * @param p2
     *            is the another point indicating the direction of the arrow.
     * @param d 
     */
    protected void paintArrow(final Point p1, final Point p2, final Graphics2D g, final double zoomFactor) {
    	
    	double dx, dy, dxn, dyn;
    	dx = p2.x - p1.x; /* direction of p1 -> p3 */
    	dy = p2.y - p1.y;
    	final double length = Math.sqrt(dx * dx + dy * dy) / zoomFactor;
    	dxn = dx / length; /* normalized direction of p1 -> p3 */
    	dyn = dy / length;
    	final double width = .5f;
    	final Polygon p = new Polygon();
    	p.addPoint((p1.x), (p1.y));
    	p.addPoint((int) (p1.x + dxn + width * dyn), (int) (p1.y + dyn - width * dxn));
    	p.addPoint((int) (p1.x + dxn - width * dyn), (int) (p1.y + dyn + width * dxn));
    	p.addPoint((p1.x), (p1.y));
    	g.fillPolygon(p);
    }

	NodeView getSource() {
    	return source;
    }

	NodeView getTarget() {
    	return target;
    }

	/**
     */
    protected boolean isSourceVisible() {
    	return (source != null && source.isContentVisible());
    }

	/**
     */
    protected boolean isTargetVisible() {
    	return (target != null && target.isContentVisible());
    }

	public AConnectorView(final ConnectorModel connectorModel, final NodeView source, final NodeView target) {
		super();
		this.connectorModel = connectorModel;
		this.source = source;
		this.target = target;
	}
}
