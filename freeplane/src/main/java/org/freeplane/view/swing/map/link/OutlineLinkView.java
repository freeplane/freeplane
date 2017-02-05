/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Line2D;

import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.edge.EdgeView;

/**
 * @author Dimitry Polivaev
 * 29.08.2009
 */
public class OutlineLinkView extends EdgeView {
	private int xs[];
	private int ys[];

	public OutlineLinkView(NodeView source, NodeView target, Component paintedComponent) {
	    super(source, target, paintedComponent);
    }

	@Override
	public boolean detectCollision(final Point p) {
		final CollisionDetector collisionDetector = new CollisionDetector();
		for (int i = 1; i < xs.length; i++) {
			if (collisionDetector.detectCollision(p, new Line2D.Float(xs[i - 1], ys[i - 1], xs[i], ys[i]))) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void createStart() {
		final MainView startMainView = getSource().getMainView();
		start = new Point(startMainView.getWidth(), startMainView.getHeight() / 2);
		final MainView targetMainView = getTarget().getMainView();
		end = new Point(targetMainView.getWidth(), targetMainView.getHeight() / 2);
	}

	@Override
	protected void draw(final Graphics2D g) {
		final Color color = getColor();
		g.setColor(color);
		final Stroke stroke = getStroke();
		g.setStroke(stroke);
		final int xMiddle = Math.max(start.x, end.x) + getSource().getMap().getZoomed(10);
		xs = new int[] { start.x, xMiddle, xMiddle, end.x };
		ys = new int[] { start.y, start.y, end.y, end.y };
		g.drawPolyline(xs, ys, 4);
}

	@Override
	protected Stroke getStroke() {
		return getStroke(getWidth());
	}
}
