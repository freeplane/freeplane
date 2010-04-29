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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.link.CollisionDetector;

/**
 * This class represents a sharp Edge of a MindMap.
 */
public class SharpBezierEdgeView extends EdgeView {
	private static final float XCTRL = 12;
	private int deltaX;
	private int deltaY;
	Point2D.Float one, two;

	public SharpBezierEdgeView(final NodeView source, final NodeView target) {
		super(source, target);
	}

	public SharpBezierEdgeView(final NodeView target) {
		super(target);
	}

	@Override
	protected void createStart() {
		if (getSource().isRoot()) {
			start = getSource().getMainViewOutPoint(getTarget(), end);
			final MainView mainView = getSource().getMainView();
			final double w = mainView.getWidth() / 2;
			final double x0 = start.x - w;
			final double w2 = w * w;
			final double x02 = x0 * x0;
			if (Double.compare(w2, x02) == 0) {
				final int delta = getMap().getZoomed(getWidth() + 1);
				deltaX = 0;
				deltaY = delta;
			}
			else {
				final double delta = getMap().getZoom() * (getWidth() + 1);
				final int h = mainView.getHeight() / 2;
				final int y0 = start.y - h;
				final double k = h / w * x0 / Math.sqrt(w2 - x02);
				final double dx = delta / Math.sqrt(1 + k * k);
				deltaX = (int) dx;
				deltaY = (int) (k * dx);
				if (y0 > 0) {
					deltaY = -deltaY;
				}
			}
			UITools.convertPointToAncestor(mainView, start, getSource());
		}
		else {
			final int delta = getMap().getZoomed(getWidth() + 1);
			super.createStart();
			deltaX = 0;
			deltaY = delta;
		}
	}

	@Override
	public Stroke getStroke() {
		return EdgeView.DEF_STROKE;
	}

	@Override
	protected void draw(final Graphics2D g) {
		final GeneralPath graph = update();
		g.setColor(getColor());
		g.setPaint(getColor());
		g.setStroke(getStroke());
		g.fill(graph);
		g.draw(graph);
		//		g.setColor(Color.WHITE);
		//		g.drawOval(start.x, start.y, 4, 4);
		//		g.drawOval((int)one.x, (int)one.y, 4, 4);
		//		g.drawOval((int)two.x, (int)two.y, 4, 4);
		//		g.drawOval(end.x, end.y, 4, 4);
	}

	private GeneralPath update() {
		final float zoom = getMap().getZoom();
		float xctrlRelative = SharpBezierEdgeView.XCTRL * zoom;
		if (getTarget().isLeft()) {
			xctrlRelative = -xctrlRelative;
		}
		one = new Point2D.Float(start.x + xctrlRelative, start.y);
		two = new Point2D.Float(end.x - xctrlRelative, end.y);
		final float w = (getWidth() / 2f + 1) * zoom;
		final float w2 = w / 2;
		final CubicCurve2D.Float line1 = new CubicCurve2D.Float();
		final CubicCurve2D.Float line2 = new CubicCurve2D.Float();
		final float wEnd = deltaY > 0 ? w2 : -w2;
		line1.setCurve(start.x - deltaX, start.y - deltaY, one.x - deltaX, one.y - deltaY, two.x, two.y - wEnd, end.x,
		    end.y - wEnd / 4);
		line2.setCurve(end.x, end.y + wEnd / 4, two.x, two.y + wEnd, one.x + deltaX, one.y + deltaY, start.x + deltaX,
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
