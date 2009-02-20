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
package org.freeplane.view.swing.map.cloud;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.QuadCurve2D;
import java.util.LinkedList;
import java.util.Vector;

import org.freeplane.core.model.NodeModel;
import org.freeplane.features.common.cloud.CloudController;
import org.freeplane.features.common.cloud.CloudModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * This class represents a Cloud around a node.
 */
public class CloudView {
	static final Stroke DEF_STROKE = new BasicStroke(1);
	static private CloudView heightCalculator = new CloudView(null, null);

	/** the layout functions can get the additional height of the clouded node . */
	static public int getAdditionalHeigth(final CloudModel cloudModel, final NodeView source) {
		CloudView.heightCalculator.cloudModel = cloudModel;
		CloudView.heightCalculator.source = source;
		return (int) (1.1 * CloudView.heightCalculator.getDistanceToConvexHull());
	}

	protected CloudModel cloudModel;
	protected NodeView source;

	public CloudView(final CloudModel cloudModel, final NodeView source) {
		this.cloudModel = cloudModel;
		this.source = source;
	}

	public Color getColor() {
		final NodeModel model = source.getModel();
		return CloudController.getController(source.getMap().getModeController()).getColor(model);
	}

	private double getDistanceToConvexHull() {
		return 40 / (getIterativeLevel() + 1) * getZoom();
	}

	public Color getExteriorColor(final Color color) {
		return color.darker();
	}

	/**
	 * getIterativeLevel() describes the n-th nested cloud that is to be
	 * painted.
	 */
	protected int getIterativeLevel() {
		return cloudModel.getIterativeLevel(source.getModel());
	}

	protected MapView getMap() {
		return source.getMap();
	}

	protected CloudModel getModel() {
		return cloudModel;
	}

	/**
	 * Get the width in pixels rather than in width constant (like -1)
	 */
	public int getRealWidth() {
		final int width = getWidth();
		return (width < 1) ? 1 : width;
	}

	public Stroke getStroke() {
		final int width = getWidth();
		if (width < 1) {
			return CloudView.DEF_STROKE;
		}
		return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	}

	public int getWidth() {
		final NodeModel node = source.getModel();
		return CloudController.getController(source.getMap().getModeController()).getWidth(node);
	}

	protected double getZoom() {
		return getMap().getZoom();
	}

	public void paint(final Graphics graphics) {
		final Graphics2D g = (Graphics2D) graphics.create();
		final Graphics2D gstroke = (Graphics2D) g.create();
		final Color color = getColor();
		g.setColor(color);
		/* set a bigger stroke to prevent not filled areas. */
		g.setStroke(getStroke());
		/* now bold */
		gstroke.setColor(getExteriorColor(color));
		gstroke.setStroke(getStroke());
		/*
		 * calculate the distances between two points on the convex hull
		 * depending on the getIterativeLevel().
		 */
		double distanceBetweenPoints = 3 * getDistanceToConvexHull();
		if (getIterativeLevel() > 4) {
			distanceBetweenPoints = 100 * getZoom(); /* flat */
		}
		final double distanceToConvexHull = getDistanceToConvexHull();
		/** get coordinates */
		final LinkedList coordinates = new LinkedList();
		final ConvexHull hull = new ConvexHull();
		source.getCoordinates(coordinates);
		final Vector/* <Point> */res = hull.calculateHull(coordinates);
		final Polygon p = new Polygon();
		for (int i = 0; i < res.size(); ++i) {
			final Point pt = (Point) res.get(i);
			p.addPoint(pt.x, pt.y);
		}
		g.fillPolygon(p);
		g.drawPolygon(p);
		/* ok, now the arcs: */
		final Point lastPoint = new Point((Point) res.get(0));
		double x0, y0;
		x0 = lastPoint.x;
		y0 = lastPoint.y;
		/* close the path: */
		res.add(res.get(0));
		double x2, y2; /* the drawing start points. */
		x2 = x0;
		y2 = y0;
		for (int i = res.size() - 1; i >= 0; --i) {
			final Point nextPoint = new Point((Point) res.get(i));
			double x1, y1, x3, y3, dx, dy, dxn, dyn;
			x1 = nextPoint.x;
			y1 = nextPoint.y;
			dx = x1 - x0; /* direction of p0 -> p1 */
			dy = y1 - y0;
			final double length = Math.sqrt(dx * dx + dy * dy);
			dxn = dx / length; /* normalized direction of p0 -> p1 */
			dyn = dy / length;
			if (length > distanceBetweenPoints) {
				for (int j = 0; j < length / distanceBetweenPoints - 1; ++j) {
					if ((j + 2) * distanceBetweenPoints < length) {
						x3 = x0 + (j + 1) * distanceBetweenPoints * dxn;
						/* the drawing end point.*/
						y3 = y0 + (j + 1) * distanceBetweenPoints * dyn;
					}
					else {
						/* last point */
						x3 = x1;
						y3 = y1;
					}
					paintClouds(g, gstroke, x2, y2, x3, y3, distanceToConvexHull);
					x2 = x3;
					y2 = y3;
				}
			}
			else {
				paintClouds(g, gstroke, x2, y2, x1, y1, distanceToConvexHull);
				x2 = x1;
				y2 = y1;
			}
			x0 = x1;
			y0 = y1;
		}
		g.dispose();
	}

	private void paintClouds(final Graphics2D g, final Graphics2D gstroke, final double x0, final double y0,
	                         final double x1, final double y1, final double distanceToConvexHull) {
		double x2, y2, dx, dy;
		dx = x1 - x0;
		dy = y1 - y0;
		final double length = Math.sqrt(dx * dx + dy * dy);
		if (length == 0f) {
			return;
		}
		double dxn, dyn;
		dxn = dx / length;
		dyn = dy / length;
		x2 = x0 + .5f * dx - distanceToConvexHull * dyn;
		y2 = y0 + .5f * dy + distanceToConvexHull * dxn;
		final Shape shape = new QuadCurve2D.Double(x0, y0, x2, y2, x1, y1);
		g.fill(shape);
		gstroke.draw(shape);
	}
}
