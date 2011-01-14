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
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.common.link.ArrowType;
import org.freeplane.features.common.link.ConnectorModel;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.link.NodeLinkModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * This class represents a ArrowLink around a node.
 */
public class ConnectorView extends AConnectorView{
	private static final float[] DOTTED_DASH = new float[] { 2, 7};
	static final Stroke DEF_STROKE = new BasicStroke(1);
	private static final int LABEL_SHIFT = 4;
	private static final double PRECISION = 2;
	private Shape arrowLinkCurve;
	final private Color textColor;
	final private Color color;
	final private BasicStroke stroke;
	/* Note, that source and target are nodeviews and not nodemodels!. */
	public ConnectorView(final ConnectorModel connectorModel, final NodeView source, final NodeView target) {
		super(connectorModel, source, target);
		final LinkController linkController = LinkController.getController(getModeController());
		textColor = linkController.getColor(connectorModel);
		final int alpha = linkController.getAlpha(connectorModel);
		color =  ColorUtils.createColor(textColor, alpha);

		final int width = linkController.getWidth(connectorModel);
		if (!isSourceVisible() || !isTargetVisible()) {
			float[] dash = zoomDash(DOTTED_DASH);
			stroke = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, dash, 0);
		}
		else{
			stroke = UITools.createStroke(width, linkController.getDash(connectorModel));
		}

	}

	public float[] zoomDash(float[] dash) {
		float[] result = dash.clone();
	    final double zoom = getZoom();
	    for(float f : result){
	    	f *= zoom;
	    }
	    return result;
    }

	/**
	 */
	private Point calcInclination(final NodeView node, final int dellength) {
		return new Point(dellength, 0);
	}

	/* (non-Javadoc)
	 * @see org.freeplane.view.swing.map.link.ILinkView#detectCollision(java.awt.Point, boolean)
	 */
	public boolean detectCollision(final Point p, final boolean selectedOnly) {
		if (selectedOnly && (source == null || !source.isSelected()) && (target == null || !target.isSelected())) {
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

	Shape getArrowLinkCurve() {
		return arrowLinkCurve;
	}

	NodeLinkModel getArrowLinkModel() {
		return connectorModel;
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
		final double halfLength = getHalfLength();
		final PathIterator pathIterator = arrowLinkCurve.getPathIterator(new AffineTransform(), PRECISION);
		double lastCoords[] = new double[6];
		pathIterator.currentSegment(lastCoords);
		double length = 0;
		for (;;) {
			pathIterator.next();
			final double nextCoords[] = new double[6];
			if (pathIterator.isDone() || PathIterator.SEG_CLOSE == pathIterator.currentSegment(nextCoords)) {
				break;
			}
			final double dx = nextCoords[0] - lastCoords[0];
			final double dy = nextCoords[1] - lastCoords[1];
			final double dr = Math.sqrt(dx * dx + dy * dy);
			length += dr;
			if (length >= halfLength) {
				final double k;
				if (dr < 1) {
					k = 0.5;
				}
				else {
					k = (length - halfLength) / dr;
				}
				return new Point((int) Math.rint(nextCoords[0] - k * dx), (int) Math.rint(nextCoords[1] - k * dy));
			}
			lastCoords = nextCoords;
		}
		throw new RuntimeException("center point not found");
	}

	private double getHalfLength() {
		final PathIterator pathIterator = arrowLinkCurve.getPathIterator(new AffineTransform(), PRECISION);
		double lastCoords[] = new double[6];
		pathIterator.currentSegment(lastCoords);
		double length = 0;
		for (;;) {
			pathIterator.next();
			final double nextCoords[] = new double[6];
			if (pathIterator.isDone() || PathIterator.SEG_CLOSE == pathIterator.currentSegment(nextCoords)) {
				break;
			}
			final double dx = nextCoords[0] - lastCoords[0];
			final double dy = nextCoords[1] - lastCoords[1];
			length += Math.sqrt(dx * dx + dy * dy);
			lastCoords = nextCoords;
		}
		return length / 2;
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
	public ConnectorModel getModel() {
		return connectorModel;
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
		final Color oldColor = g.getColor();
		g.setColor(color);
		/* set stroke. */
		g.setStroke(stroke);
		if (isSourceVisible()) {
			p1 = source.getLinkPoint(connectorModel.getStartInclination());
			sourceIsLeft = source.isLeft();
		}
		if (isTargetVisible()) {
			p2 = target.getLinkPoint(connectorModel.getEndInclination());
			targetIsLeft = target.isLeft();
		}
		if (connectorModel.getEndInclination() == null || connectorModel.getStartInclination() == null) {
			final int dellength = isSourceVisible() && isTargetVisible() ? Math.max(40, (int)(p1.distance(p2) / getZoom())) : 40;
			if (isSourceVisible() && connectorModel.getStartInclination() == null) {
				final Point incl = calcInclination(source, dellength);
				connectorModel.setStartInclination(incl);
				p1 = source.getLinkPoint(connectorModel.getStartInclination());
			}
			if (isTargetVisible() && connectorModel.getEndInclination() == null) {
				final Point incl = calcInclination(target, dellength);
				incl.y = -incl.y;
				connectorModel.setEndInclination(incl);
				p2 = target.getLinkPoint(connectorModel.getEndInclination());
			}
		}
		if (p1 != null) {
			p3 = new Point(p1);
			p3.translate(((sourceIsLeft) ? -1 : 1) * getMap().getZoomed(connectorModel.getStartInclination().x),
				getMap().getZoomed(connectorModel.getStartInclination().y));

		}
		if (p2 != null) {
			p4 = new Point(p2);
			p4.translate(((targetIsLeft) ? -1 : 1) * getMap().getZoomed(connectorModel.getEndInclination().x), getMap()
				.getZoomed(connectorModel.getEndInclination().y));
		}
		paintCurve(g, p1, p2, p3, p4);
		drawLabels(g, p1, p2, p3, p4);
		g.setColor(oldColor);
	}

	private Shape createLine(Point p1, Point p2, Point p3, Point p4) {
	    return new Line2D.Float(p1, p2);
    }

	private Shape createLinearPath(Point p1, Point p2, Point p3, Point p4) {
	    final GeneralPath generalPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, 4);
	    generalPath.moveTo(p1.x, p1.y);
	    generalPath.lineTo(p3.x, p3.y);
	    generalPath.lineTo(p4.x, p4.y);
	    generalPath.lineTo(p2.x, p2.y);
		return generalPath;
    }

	private void paintCurve(final Graphics2D g, Point p1, Point p2, Point p3, Point p4) {
		final boolean isLine = ConnectorModel.Shape.LINE.equals(connectorModel.getShape());
		if (p1 != null && p2 != null) {
			if(isLine)
				arrowLinkCurve = createLine(p1, p2, p3, p4);
			else if (ConnectorModel.Shape.LINEAR_PATH.equals(connectorModel.getShape()))
				arrowLinkCurve = createLinearPath(p1, p2, p3, p4);
			else
				arrowLinkCurve = createCubicCurve2D(p1, p2, p3, p4);
		}
		else
			arrowLinkCurve = null;
	    if (arrowLinkCurve != null) {
			g.draw(arrowLinkCurve);
		}
		if (isSourceVisible() && !connectorModel.getStartArrow().equals(ArrowType.NONE)) {
			if(isLine && p2 != null)
				paintArrow(g, p1, p2);
			else
				paintArrow(g, p1, p3);
		}
		if (isTargetVisible() && !connectorModel.getEndArrow().equals(ArrowType.NONE)) {
			if(isLine && p1 != null)
				paintArrow(g, p2, p1);
			else
			paintArrow(g, p2, p4);
		}
		if (connectorModel.getShowControlPointsFlag()) {
			g.setColor(textColor);
			g.setStroke(new BasicStroke(stroke.getLineWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, DOTTED_DASH, 0));
		}
		if (connectorModel.getShowControlPointsFlag() || !isSourceVisible() || !isTargetVisible()) {
			if (p1 != null) {
				g.drawLine(p1.x, p1.y, p3.x, p3.y);
			}
			if (p2 != null) {
				g.drawLine(p2.x, p2.y, p4.x, p4.y);
			}
		}
    }

	private void paintArrow(final Graphics2D g, Point p1, Point p2) {
	    paintArrow(p1, p2, g, getZoom() * 10, (int) Math.ceil(0.5 + stroke.getLineWidth() / 4));
    }

	private void drawLabels(final Graphics2D g, Point p1, Point p2, Point p3, Point p4) {
	    final String sourceLabel = connectorModel.getSourceLabel();
		final String middleLabel = connectorModel.getMiddleLabel();
		final String targetLabel = connectorModel.getTargetLabel();
		g.setColor(textColor);
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

	private CubicCurve2D createCubicCurve2D(Point p1, Point p2, Point p3, Point p4) {
	    final CubicCurve2D arrowLinkCurve = new CubicCurve2D.Double();
		if (p1 != null && p2 != null) {
			arrowLinkCurve.setCurve(p1, p3, p4, p2);
		}
		else if (p1 != null) {
			arrowLinkCurve.setCurve(p1, p3, p1, p3);
		}
		else if (p2 != null) {
			arrowLinkCurve.setCurve(p2, p4, p2, p4);
		}
	    return arrowLinkCurve;
    }

	/* (non-Javadoc)
	 * @see org.freeplane.view.swing.map.link.ILinkView#increaseBounds(java.awt.Rectangle)
	 */
	public void increaseBounds(final Rectangle innerBounds) {
		final Shape arrowLinkCurve = getArrowLinkCurve();
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
