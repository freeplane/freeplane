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

import org.freeplane.features.link.ConnectorModel;
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

	protected int getZoomed(int i) {
		return getMap().getZoomed(i);
	}
	protected MapView getMap() {
		return (source == null) ? target.getMap() : source.getMap();
	}

	/**
     * @param from
     *            is the another point indicating the direction of the arrow.
	 * @param to
     *            is the start point
	 * @param d 
     */
    protected void paintArrow(final Point from, final Point to, final Graphics2D g, final double size) {
    	int dx, dy;
    	double dxn, dyn;
    	dx = from.x - to.x;
    	dy = from.y - to.y;
    	final int r2 = dx * dx + dy * dy;
    	if(r2 == 0)
    		return;
		final double length = Math.sqrt(r2);
    	dxn = size * dx / length;
    	dyn = size * dy / length;
    	final double arrowWidth = .5f;
    	final Polygon p = new Polygon();
    	p.addPoint((to.x), (to.y));
    	p.addPoint((int) (to.x + dxn + arrowWidth * dyn), (int) (to.y + dyn - arrowWidth * dxn));
    	p.addPoint((int) (to.x + dxn - arrowWidth * dyn), (int) (to.y + dyn + arrowWidth * dxn));
    	p.addPoint((to.x), (to.y));
    	g.fillPolygon(p);
    	g.drawPolygon(p);
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
