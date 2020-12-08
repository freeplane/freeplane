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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;

import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.link.CollisionDetector;

/**
 * This class represents a single Edge of a MindMap.
 */
public class BezierEdgeView extends EdgeView {
	private static final int CHILD_XCTRL = 20;
	private static final int XCTRL = 12;

	public BezierEdgeView(NodeView source, NodeView target, Component paintedComponent) {
	    super(source, target, paintedComponent);
    }

	@Override
	protected void draw(final Graphics2D g) {
		final CubicCurve2D.Float graph = update();
		final Color color = getColor();
		g.setColor(color);
		final Stroke stroke = getStroke();
		g.setStroke(stroke);
		g.draw(graph);
		if (drawHiddenParentEdge()) {
			g.setColor(g.getBackground());
			g.setStroke(EdgeView.getEclipsedStroke());
			g.draw(graph);
			g.setStroke(stroke);
			g.setColor(color);
		}
	}

	private CubicCurve2D.Float update() {
        final Point startControlPoint = getControlPoint(getStartConnectorLocation());
        final int zoomedXCTRL = getMap().getZoomed(XCTRL);
        final int xctrl = startControlPoint.x * zoomedXCTRL; 
        final int yctrl = startControlPoint.y * zoomedXCTRL; 
        final Point endControlPoint = getControlPoint(getEndConnectorLocation());
        final int zoomedChildXCTRL = getMap().getZoomed(CHILD_XCTRL);
        final int childXctrl = endControlPoint.x * zoomedChildXCTRL; 
        final int childYctrl = endControlPoint.y * zoomedChildXCTRL; 
		final CubicCurve2D.Float graph = new CubicCurve2D.Float();
		graph.setCurve(start.x, start.y, start.x + xctrl, start.y + yctrl, end.x + childXctrl, end.y  + childYctrl, end.x, end.y);
		return graph;
	}
	
	@Override
	public boolean detectCollision(final Point p) {
		final CubicCurve2D.Float graph = update();
		return new CollisionDetector().detectCollision(p, graph);
	}
}
