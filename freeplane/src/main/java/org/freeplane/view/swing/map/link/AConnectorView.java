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
import java.awt.geom.Point2D;

import org.freeplane.features.link.ConnectorModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 * Apr 29, 2010
 */
abstract class AConnectorView  implements ILinkView {
    enum ArrowDirection {INCOMING, OUTGOING}
	protected final ConnectorModel viewedConnector;
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

	protected void paintArrow(final Point from, final Point to, final Graphics2D g, final double size, ArrowDirection direction) {
	    final Polygon p = createArrowShape(from, to, size, direction);
	    g.fillPolygon(p);
	    g.drawPolygon(p);
	}

    private Polygon createArrowShape(final Point from, final Point to, final double size, ArrowDirection direction) {
        Point2D directionPoint = createArrowDirection(from, to, size);
        return direction == ArrowDirection.INCOMING ? incomingArrowShape(to, directionPoint) : outgoingArrowShape(to, directionPoint);
    }

    private Point2D createArrowDirection(final Point from, final Point to, final double size) {
        int dx, dy;
    	double dxn, dyn;
    	dx = from.x - to.x;
    	dy = from.y - to.y;
    	final int r2 = dx * dx + dy * dy;
    	if(r2 == 0)
    		return null;
		final double length = Math.sqrt(r2);
    	dxn = size * dx / length;
    	dyn = size * dy / length;
    	Point2D direction = new Point2D.Double(dxn, dyn);
        return direction;
    }

    private Polygon outgoingArrowShape(final Point from, Point2D direction ) {
        final Polygon p;
        final double arrowWidth = .33d;
        double dxn = direction.getX();
        double dyn = direction.getY();
        p = new Polygon();
        p.addPoint((int) (from.x + dxn), (int) (from.y + dyn));
        p.addPoint((int) (from.x + arrowWidth * dyn), (int) (from.y - arrowWidth * dxn));
        p.addPoint((int) (from.x + dxn*0.7), (int) (from.y + dyn*0.7));
        p.addPoint((int) (from.x - arrowWidth * dyn), (int) (from.y + arrowWidth * dxn));
        p.addPoint((int) (from.x + dxn), (int) (from.y + dyn));
        return p;
    }

    private Polygon incomingArrowShape(final Point to, Point2D direction ) {
        final Polygon p;
        final double arrowWidth = .5d;
        double dxn = direction.getX();
        double dyn = direction.getY();
        p = new Polygon();
        p.addPoint((to.x), (to.y));
        p.addPoint((int) (to.x + dxn + arrowWidth * dyn), (int) (to.y + dyn - arrowWidth * dxn));
        p.addPoint((int) (to.x + dxn * 0.8d), (int) (to.y + dyn * 0.8d));
        p.addPoint((int) (to.x + dxn - arrowWidth * dyn), (int) (to.y + dyn + arrowWidth * dxn));
        p.addPoint((to.x), (to.y));
        return p;
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
		this.viewedConnector = connectorModel;
		this.source = source;
		this.target = target;
	}
}
