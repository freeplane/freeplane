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
import java.awt.Polygon;
import java.awt.Stroke;

import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.link.CollisionDetector;

/**
 * This class represents a sharp Edge of a MindMap.
 */
public class SharpLinearEdgeView extends SharpEdgeView {
	public SharpLinearEdgeView(NodeView source, NodeView target, Component paintedComponent) {
	    super(source, target, paintedComponent);
    }

	@Override
	public Stroke getStroke() {
		return getStroke(0);
	}

	@Override
	protected void draw(final Graphics2D g) {
		g.setColor(getColor());
		g.setPaint(getColor());
		g.setStroke(getStroke());
        final int deltaX = getDeltaX();
        final int deltaY = getDeltaY();
		final int xs[] = { start.x + deltaX, end.x, start.x - deltaX};
		final int ys[] = { start.y + deltaY, end.y, start.y - deltaY };
		g.fillPolygon(xs, ys, 3);
	}

	@Override
	public boolean detectCollision(final Point p) {
		final int w = getMap().getZoomed(getWidth() / 2 + 1);
		final int xs[] = { start.x, end.x, start.x };
		final int ys[] = { start.y + w, end.y, start.y - w };
		final Polygon polygon = new Polygon(xs, ys, 3);
		return new CollisionDetector().detectCollision(p, polygon);
	}
}
