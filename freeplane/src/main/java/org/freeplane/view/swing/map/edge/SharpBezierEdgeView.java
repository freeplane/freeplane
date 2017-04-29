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
package org.freeplane.view.swing.map.edge;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.link.CollisionDetector;

/**
 * This class represents a sharp Edge of a MindMap.
 */
public class SharpBezierEdgeView extends SharpEdgeView {
	private static final float XCTRL = 12;
	Point2D.Float one, two;
	public SharpBezierEdgeView(NodeView source, NodeView target, Component paintedComponent) {
	    super(source, target, paintedComponent);
    }

	@Override
	public Stroke getStroke() {
		return getStroke(0);
	}

	@Override
	protected void draw(final Graphics2D g) {
		final GeneralPath graph = update();
		g.setColor(getColor());
		g.setPaint(getColor());
		g.setStroke(getStroke());
		g.fill(graph);
		g.draw(graph);
	}

	private GeneralPath update() {
        final Point startControlPoint = getControlPoint(getStartConnectorLocation());
        final float zoom = getMap().getZoom();
        final float zoomedXCTRL = zoom * XCTRL;
        final float xctrl = startControlPoint.x * zoomedXCTRL; 
        final float yctrl = startControlPoint.y * zoomedXCTRL; 
        final Point endControlPoint = getControlPoint(getEndConnectorLocation());
        final float w = (getWidth() / 2f + 1) * zoom;
        final float w2 = w / 2;
        final int deltaX = getDeltaX();
        final int deltaY = getDeltaY();
        final float childXctrl = deltaX > 0 ? endControlPoint.y * w2 : -endControlPoint.y * w2; 
        final float childYctrl = deltaY > 0 ? endControlPoint.x * w2 : -endControlPoint.x * w2; 
	    
		one = new Point2D.Float(start.x + xctrl, start.y + yctrl);
		two = new Point2D.Float(end.x - xctrl, end.y - yctrl);
		final CubicCurve2D.Float line1 = new CubicCurve2D.Float();
		final CubicCurve2D.Float line2 = new CubicCurve2D.Float();
		line1.setCurve(start.x - deltaX, start.y - deltaY, one.x - deltaX, one.y - deltaY, two.x - childXctrl, two.y - childYctrl, end.x - childXctrl/4,
		    end.y - childYctrl / 4);
		line2.setCurve(end.x + childXctrl/4, end.y + childYctrl / 4, two.x  + childXctrl, two.y + childYctrl, one.x + deltaX, one.y + deltaY, start.x + deltaX,
		    start.y + deltaY);
		final GeneralPath graph = new GeneralPath();
		graph.append(line1, true);
		graph.append(line2, true);
		graph.closePath();
		return graph;
	}

	@Override
	public boolean detectCollision(final Point p) {
		final CubicCurve2D.Float line1 = new CubicCurve2D.Float();
		line1.setCurve(start.x, start.y, one.x, one.y, two.x, two.y, end.x, end.y);
		return new CollisionDetector().detectCollision(p, line1);
	}
}
