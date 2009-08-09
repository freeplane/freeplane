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
package org.freeplane.view.swing.map.link;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.features.common.link.ArrowLinkModel;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.link.NodeLinkModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * This class represents a ArrowLink around a node.
 */
public class ArrowLinkView implements ILinkView {
	static final Stroke DEF_STROKE = new BasicStroke(1);
	private static final int LABEL_SHIFT = 4;
	private CubicCurve2D arrowLinkCurve;
	private final ArrowLinkModel arrowLinkModel;
	private final NodeView source, target;

	/* Note, that source and target are nodeviews and not nodemodels!. */
	public ArrowLinkView(final ArrowLinkModel arrowLinkModel, final NodeView source, final NodeView target) {
		this.arrowLinkModel = arrowLinkModel;
		this.source = source;
		this.target = target;
	}

	/**
	 */
	private Point calcInclination(final NodeView node, final double dellength) {
		/*
		 * int w = node.getWidth(); int h = node.getHeight(); double r =
		 * Math.sqrt(ww+hh); double wr = dellength w / r; double hr = dellength
		 * h / r; return new Point((int)wr, (int)hr);
		 */
		return new Point((int) dellength, 0);
	}

	/* (non-Javadoc)
     * @see org.freeplane.view.swing.map.link.ILinkView#detectCollision(java.awt.Point, boolean)
     */
	public boolean detectCollision(final Point p, boolean selectedOnly) {
		if(selectedOnly && (source == null ||  !source.isSelected()) && (target == null || !target.isSelected())){
			return false;
		}
		if (arrowLinkCurve == null) {
			return false;
		}
		return new CollisionDetector().detectCollision(p, arrowLinkCurve);
	}
	
	private void drawEndPointText(final Graphics2D g, final String text, final Point endPoint, final Point controlPoint) {
		if (text == null || text.equals("")) {
			return;
		}
		final FontMetrics fontMetrics = g.getFontMetrics();
		final int textWidth = fontMetrics.stringWidth(text);
		final int textHeight = fontMetrics.getMaxAscent();
		final int x;
		if (controlPoint.x > endPoint.x) {
			x = endPoint.x - textWidth;
		}
		else {
			x = endPoint.x;
		}
		final int y;
		if (controlPoint.y > endPoint.y) {
			y = endPoint.y + textHeight + LABEL_SHIFT;
		}
		else {
			y = endPoint.y - LABEL_SHIFT;
		}
		g.drawString(text, x, y);
	}

	private void drawMiddleLabel(final Graphics2D g, final String middleLabel) {
		if (middleLabel == null || middleLabel.equals("")) {
			return;
		}
		final FontMetrics fontMetrics = g.getFontMetrics();
		final int textWidth = fontMetrics.stringWidth(middleLabel);
		final Point centerPoint = getCenterPoint();
		g.drawString(middleLabel, centerPoint.x - textWidth / 2, centerPoint.y - LABEL_SHIFT);
	}

	CubicCurve2D getArrowLinkCurve() {
		return arrowLinkCurve;
	}

	NodeLinkModel getArrowLinkModel() {
		return arrowLinkModel;
	}

	Rectangle getBounds() {
		if (arrowLinkCurve == null) {
			return new Rectangle();
		}
		return arrowLinkCurve.getBounds();
	}

	Point getCenterPoint() {
		if (arrowLinkCurve == null) {
			return null;
		}
		final Point2D p1 = arrowLinkCurve.getP1();
		final Point2D p2 = arrowLinkCurve.getP2();
		final Point2D center = new Point2D.Double((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2);
		final Point2D normal = normal(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		// flatten the curve and test for intersection (bug fix, fc, 16.1.2004).
		final FlatteningPathIterator pi = new FlatteningPathIterator(arrowLinkCurve.getPathIterator(null), 2, 10/*=maximal 2^10=1024 points.*/);
		double oldCoordinateX = 0, oldCoordinateY = 0;
		while (pi.isDone() == false) {
			final double[] coordinates = new double[6];
			final int type = pi.currentSegment(coordinates);
			switch (type) {
				case PathIterator.SEG_LINETO:
					final Point centerPoint = intersection(oldCoordinateX, oldCoordinateY, coordinates[0],
					    coordinates[1], center.getX(), center.getY(), center.getX() + 1024 * normal.getX(), center
					        .getY()
					            + 1024 * normal.getY());
					if (centerPoint != null) {
						return centerPoint;
					}
					/* this case needs the same action as the next case, thus no "break" */
				case PathIterator.SEG_MOVETO:
					oldCoordinateX = coordinates[0];
					oldCoordinateY = coordinates[1];
					break;
			}
			pi.next();
		}
		throw new RuntimeException("center point not found");
	}

	Color getColor() {
		final ArrowLinkModel model = getModel();
		return LinkController.getController(getModeController()).getColor(model);
	}

	protected MapView getMap() {
		return (source == null) ? target.getMap() : source.getMap();
	}

	private ModeController getModeController() {
		NodeView nodeView = source;
		if (source == null) {
			nodeView = target;
		}
		final MapView mapView = nodeView.getMap();
		return mapView.getModeController();
	}

	/* (non-Javadoc)
     * @see org.freeplane.view.swing.map.link.ILinkView#getModel()
     */
	public ArrowLinkModel getModel() {
		return arrowLinkModel;
	}

	/**
	 * Get the width in pixels rather than in width constant (like -1)
	 */
	int getRealWidth() {
		final int width = getWidth();
		return (width < 1) ? 1 : width;
	}

	NodeView getSource() {
		return source;
	}

	Stroke getStroke() {
		final int width = getWidth();
		if (width < 1) {
			return ArrowLinkView.DEF_STROKE;
		}
		return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	}

	NodeView getTarget() {
		return target;
	}

	int getWidth() {
		final NodeLinkModel model = getModel();
		return LinkController.getController(getModeController()).getWidth(model);
	}

	protected double getZoom() {
		return getMap().getZoom();
	}

	/**
	 * Computes the intersection between two lines. The calculated point is approximate, 
	 * since integers are used. If you need a more precise result, use doubles
	 * everywhere. 
	 * (c) 2007 Alexander Hristov. Use Freely (LGPL license). http://www.ahristov.com
	 *
	 * @param x1 Point 1 of Line 1
	 * @param y1 Point 1 of Line 1
	 * @param x2 Point 2 of Line 1
	 * @param y2 Point 2 of Line 1
	 * @param x3 Point 1 of Line 2
	 * @param y3 Point 1 of Line 2
	 * @param x4 Point 2 of Line 2
	 * @param y4 Point 2 of Line 2
	 * @return Point where the segments intersect, or null if they don't
	 */
	Point intersection(final double x1, final double y1, final double x2, final double y2, final double x3,
	                          final double y3, final double x4, final double y4) {
		final double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		if (d == 0) {
			return null;
		}
		final int xi = (int) (((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d);
		final int yi = (int) (((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d);
		if (xi + 2 < Math.min(x1, x2) || xi - 2 > Math.max(x1, x2)) {
			return null;
		}
		return new Point(xi, yi);
	}

	/**
	 */
	private boolean isSourceVisible() {
		return (source != null && source.isContentVisible());
	}

	/**
	 */
	private boolean isTargetVisible() {
		return (target != null && target.isContentVisible());
	}

	/**
	 * Computes the unitary normal vector of a segment
	 * @param x1 Starting point of the segment
	 * @param y1 Starting point of the segment
	 * @param x2 Ending point of the segment
	 * @param y2 Ending point of the segment
	 * @return
	 */
	Point2D.Double normal(final double x1, final double y1, final double x2, final double y2) {
		double nx, ny;
		if (x1 == x2) {
			nx = Math.signum(y2 - y1);
			ny = 0;
		}
		else {
			final double f = (y2 - y1) / (x2 - x1);
			nx = f * Math.signum(x2 - x1) / Math.sqrt(1 + f * f);
			ny = -1 * Math.signum(x2 - x1) / Math.sqrt(1 + f * f);
		}
		return new Point2D.Double(nx, ny);
	}

	/* (non-Javadoc)
     * @see org.freeplane.view.swing.map.link.ILinkView#paint(java.awt.Graphics)
     */
	public void paint(final Graphics graphics) {
		if (!isSourceVisible() && !isTargetVisible()) {
			return;
		}
		Point p1 = null, p2 = null, p3 = null, p4 = null;
		boolean targetIsLeft = false;
		boolean sourceIsLeft = false;
		final Graphics2D g = (Graphics2D) graphics.create();
		g.setColor(getColor());
		/* set stroke. */
		g.setStroke(getStroke());
		if (!isSourceVisible() || !isTargetVisible()) {
			g.setStroke(new BasicStroke(getWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] { 0,
			        3, 0, 3 }, 0));
		}
		if (isSourceVisible()) {
			p1 = source.getLinkPoint(arrowLinkModel.getStartInclination());
			sourceIsLeft = source.isLeft();
		}
		if (isTargetVisible()) {
			p2 = target.getLinkPoint(arrowLinkModel.getEndInclination());
			targetIsLeft = target.isLeft();
		}
		if (arrowLinkModel.getEndInclination() == null || arrowLinkModel.getStartInclination() == null) {
			final double dellength = isSourceVisible() && isTargetVisible() ? p1.distance(p2) / getZoom() : 30;
			if (isSourceVisible() && arrowLinkModel.getStartInclination() == null) {
				final Point incl = calcInclination(source, dellength);
				arrowLinkModel.setStartInclination(incl);
				p1 = source.getLinkPoint(arrowLinkModel.getStartInclination());
			}
			if (isTargetVisible() && arrowLinkModel.getEndInclination() == null) {
				final Point incl = calcInclination(target, dellength);
				incl.y = -incl.y;
				arrowLinkModel.setEndInclination(incl);
				p2 = target.getLinkPoint(arrowLinkModel.getEndInclination());
			}
		}
		arrowLinkCurve = new CubicCurve2D.Double();
		if (p1 != null) {
			p3 = new Point(p1);
			p3.translate(((sourceIsLeft) ? -1 : 1) * getMap().getZoomed(arrowLinkModel.getStartInclination().x),
			    getMap().getZoomed(arrowLinkModel.getStartInclination().y));
			if (p2 == null) {
				arrowLinkCurve.setCurve(p1, p3, p1, p3);
			}
		}
		if (p2 != null) {
			p4 = new Point(p2);
			p4.translate(((targetIsLeft) ? -1 : 1) * getMap().getZoomed(arrowLinkModel.getEndInclination().x), getMap()
			    .getZoomed(arrowLinkModel.getEndInclination().y));
			if (p1 == null) {
				arrowLinkCurve.setCurve(p2, p4, p2, p4);
			}
		}
		if (p1 != null && p2 != null) {
			arrowLinkCurve.setCurve(p1, p3, p4, p2);
			g.draw(arrowLinkCurve);
		}
		if (isSourceVisible() && !arrowLinkModel.getStartArrow().equals("None")) {
			paintArrow(p1, p3, g);
		}
		if (isTargetVisible() && !arrowLinkModel.getEndArrow().equals("None")) {
			paintArrow(p2, p4, g);
		}
		if (arrowLinkModel.getShowControlPointsFlag() || !isSourceVisible() || !isTargetVisible()) {
			g.setStroke(new BasicStroke(getWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] { 0,
			        3, 0, 3 }, 0));
			if (p1 != null) {
				g.drawLine(p1.x, p1.y, p3.x, p3.y);
			}
			if (p2 != null) {
				g.drawLine(p2.x, p2.y, p4.x, p4.y);
			}
		}
		final String sourceLabel = arrowLinkModel.getSourceLabel();
		final String middleLabel = arrowLinkModel.getMiddleLabel();
		final String targetLabel = arrowLinkModel.getTargetLabel();
		if (p1 != null) {
			drawEndPointText(g, sourceLabel, p1, p3);
			if (p2 == null) {
				drawEndPointText(g, middleLabel, p3, p1);
			}
		}
		if (p2 != null) {
			drawEndPointText(g, targetLabel, p2, p4);
			if (p1 == null) {
				drawEndPointText(g, middleLabel, p4, p2);
			}
		}
		if (p1 != null && p2 != null) {
			drawMiddleLabel(g, middleLabel);
		}
	}

	/**
	 * @param p1
	 *            is the start point
	 * @param p3
	 *            is the another point indicating the direction of the arrow.
	 */
	private void paintArrow(final Point p1, final Point p3, final Graphics2D g) {
		double dx, dy, dxn, dyn;
		dx = p3.x - p1.x; /* direction of p1 -> p3 */
		dy = p3.y - p1.y;
		final double length = Math.sqrt(dx * dx + dy * dy) / (getZoom() * 10/*=zoom factor for arrows*/);
		dxn = dx / length; /* normalized direction of p1 -> p3 */
		dyn = dy / length;
		final double width = .5f;
		final Polygon p = new Polygon();
		p.addPoint((p1.x), (p1.y));
		p.addPoint((int) (p1.x + dxn + width * dyn), (int) (p1.y + dyn - width * dxn));
		p.addPoint((int) (p1.x + dxn - width * dyn), (int) (p1.y + dyn + width * dxn));
		p.addPoint((p1.x), (p1.y));
		g.fillPolygon(p);
	}
	/* (non-Javadoc)
     * @see org.freeplane.view.swing.map.link.ILinkView#increaseBounds(java.awt.Rectangle)
     */
	public void increaseBounds(final Rectangle innerBounds) {
	    final CubicCurve2D arrowLinkCurve = getArrowLinkCurve();
			if (arrowLinkCurve == null) {
				return;
			}
	    final Rectangle arrowViewBigBounds = arrowLinkCurve.getBounds();
	    if (!innerBounds.contains(arrowViewBigBounds)) {
	    	final Rectangle arrowViewBounds = PathBBox.getBBox(arrowLinkCurve).getBounds();
	    	innerBounds.add(arrowViewBounds);
	    }
    }

}
