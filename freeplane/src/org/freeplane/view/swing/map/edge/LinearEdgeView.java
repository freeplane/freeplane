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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Line2D;

import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.link.CollisionDetector;

/**
 * This class represents a single Edge of a MindMap.
 */
public class LinearEdgeView extends EdgeView {
	public LinearEdgeView(NodeView source, NodeView target) {
	    super(source, target);
    }

	public LinearEdgeView(NodeView target) {
	    super(target);
    }

	@Override
	protected void draw(final Graphics2D g) {
		final Color color = getColor();
		g.setColor(color);
		final Stroke stroke = getStroke();
		g.setStroke(stroke);
		final int w = getWidth();
		if (w <= 1) {
			g.drawLine(start.x, start.y, end.x, end.y);
			if (isTargetEclipsed()) {
				g.setColor(g.getBackground());
				g.setStroke(EdgeView.getEclipsedStroke());
				g.drawLine(start.x, start.y, end.x, end.y);
				g.setColor(color);
				g.setStroke(stroke);
			}
		}
		else {
			int dx = w / 3 + 1;
			if (getTarget().isLeft()) {
				dx = -dx;
			}
			final int xs[] = { start.x, start.x + dx, end.x - dx, end.x };
			final int ys[] = { start.y, start.y, end.y, end.y };
			g.drawPolyline(xs, ys, 4);
			if (isTargetEclipsed()) {
				g.setColor(g.getBackground());
				g.setStroke(EdgeView.getEclipsedStroke());
				g.drawPolyline(xs, ys, 4);
				g.setColor(color);
				g.setStroke(stroke);
			}
		}
	}

	@Override
    public boolean detectCollision(Point p) {
	    Line2D line = new  Line2D.Float(start, end);
	    return new CollisionDetector().detectCollision(p, line);
    }
}
