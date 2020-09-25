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
import java.awt.Stroke;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

import org.freeplane.features.cloud.CloudController;
import org.freeplane.features.cloud.CloudModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * This class represents a Cloud around a node.
 */
abstract public class CloudView {
	static final Stroke DEF_STROKE = new BasicStroke(1);

	/** the layout functions can get the additional height of the clouded node .
	 * @param cloud */
	static public int getAdditionalHeigth(CloudModel cloud, final NodeView source) {
		final CloudView heightCalculator = new CloudViewFactory().createCloudView(cloud, source);
		return (int) (2.2 * heightCalculator.getDistanceToConvexHull());
	}

	protected CloudModel cloudModel;
	protected NodeView source;
	private final int iterativeLevel;
	private Random random;

	CloudView(final CloudModel cloudModel, final NodeView source) {
		this.cloudModel = cloudModel;
		this.source = source;
		iterativeLevel = getCloudIterativeLevel();
	}

	private int getCloudIterativeLevel() {
		int iterativeLevel = 0;
		for (NodeView parentNode = source.getParentView(); parentNode != null; parentNode = parentNode.getParentView()) {
			if (null != parentNode.getCloudModel()) {
				iterativeLevel++;
			}
		}
		return iterativeLevel;
    }

	public Color getColor() {
		return source.getCloudColor();
	}

	protected double getDistanceToConvexHull() {
		return 20 / (getIterativeLevel() + 1) * getZoom();
	}

	public Color getExteriorColor(final Color color) {
		return color.darker();
	}

	/**
	 * getIterativeLevel() describes the n-th nested cloud that is to be
	 * painted.
	 */
	protected int getIterativeLevel() {
		return iterativeLevel;
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
		random = new Random(0);
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
		/** get coordinates */
		paintDecoration(g, gstroke);
		g.dispose();
	}

	protected Polygon getCoordinates() {
        final Polygon p = new Polygon();
        final LinkedList<Point> coordinates = new LinkedList<Point>();
        source.getCoordinates(coordinates);
        final ConvexHull hull = new ConvexHull();
        final Vector<Point> res = hull.calculateHull(coordinates);
        Point lastPt = null;
        for (int i = 0; i < res.size(); ++i) {
            final Point pt = (Point) res.get(i);
            if(!pt.equals(lastPt)){
                p.addPoint(pt.x, pt.y);
                lastPt = pt;
            }
        }
        final Point pt = (Point) res.get(0);
        p.addPoint(pt.x, pt.y);
        return p;
	}

	protected void paintDecoration(Graphics2D g, Graphics2D gstroke){
	    Polygon p = getCoordinates();
		fillPolygon(p, g);
		double middleDistanceBetweenPoints = calcDistanceBetweenPoints();
		final int[] xpoints = p.xpoints;
		final int[] ypoints = p.ypoints;
		final Point lastPoint = new Point(xpoints[0], ypoints[0]);
		double x0, y0;
		x0 = lastPoint.x;
		y0 = lastPoint.y;
		/* close the path: */
		double x2, y2; /* the drawing start points. */
		x2 = x0;
		y2 = y0;
		for (int i = p.npoints - 2; i >= 0; --i) {
			final Point nextPoint = new Point(xpoints[i], ypoints[i]);
			double x1, y1, x3, y3, dx, dy, dxn, dyn;
			x1 = nextPoint.x;
			y1 = nextPoint.y;
			dx = x1 - x0; /* direction of p0 -> p1 */
			dy = y1 - y0;
			final double length = Math.sqrt(dx * dx + dy * dy);
			dxn = dx / length; /* normalized direction of p0 -> p1 */
			dyn = dy / length;
			for (int j = 0;;) {
				double distanceBetweenPoints = middleDistanceBetweenPoints * random(0.7);
				if (j + 2* distanceBetweenPoints < length) {
					j += distanceBetweenPoints;
					x3 = x0 + j * dxn;
					/* the drawing end point.*/
					y3 = y0 + j * dyn;
				}
				else {
					/* last point */
					break;
				}
				paintDecoration(g, gstroke, x2, y2, x3, y3);
				x2 = x3;
				y2 = y3;
			}

			paintDecoration(g, gstroke, x2, y2, x1, y1);
			x2 = x1;
			y2 = y1;
			x0 = x1;
			y0 = y1;
		}
	}

	protected void fillPolygon(final Polygon p, Graphics2D g) {
	    g.fillPolygon(p);
		g.drawPolygon(p);
    }

	protected void paintDecoration(Graphics2D g, Graphics2D gstroke, double x0, double y0, double x1, double y1) {
			double dx, dy;
			dx = x1 - x0;
			dy = y1 - y0;
			final double length = Math.sqrt(dx * dx + dy * dy);
			double dxn, dyn;
			dxn = dx / length;
			dyn = dy / length;
			paintDecoration(g, gstroke, x0, y0, x1, y1, dx, dy, dxn, dyn);
		}

	abstract protected void paintDecoration(Graphics2D g, Graphics2D gstroke, double x0, double y0, double x1, double y1,
                                 double dx, double dy, double dxn, double dyn);

    protected double calcDistanceBetweenPoints() {
	    final double distanceBetweenPoints = getDistanceToConvexHull();
		return distanceBetweenPoints;
    }

	protected double random(double min) {
	    return (min + (1-min) * random.nextDouble());
    }
}
