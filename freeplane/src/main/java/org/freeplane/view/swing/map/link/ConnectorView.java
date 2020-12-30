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
import java.awt.Font;
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
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.ArrowType;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinkModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * This class represents a ArrowLink around a node.
 */
public class ConnectorView extends AConnectorView{
	private static final int NORMAL_LENGTH = 50;
	private static final float[] DOTTED_DASH = new float[] { 4, 7};
	static final Stroke DEF_STROKE = new BasicStroke(1);
	private static final int LABEL_GAP = 4;
	private static final double PRECISION = 2;
	private Shape arrowLinkCurve;
	private Rectangle sourceTextRectangle;
	private Rectangle middleTextRectangle;
	private Rectangle targetTextRectangle;
	final private Color textColor;
	final private Color color;
	final private BasicStroke stroke;
	final private Color bgColor;
    private final LinkController linkController;
    private boolean showsControlPoints;
	/* Note, that source and target are nodeviews and not nodemodels!. */
	public ConnectorView(final ConnectorModel connectorModel, final NodeView source, final NodeView target, Color bgColor) {
		super(connectorModel, source, target);
		linkController = getLinkController();
		textColor = linkController.getColor(connectorModel);
		this.bgColor =bgColor;
		final int alpha = linkController.getOpacity(connectorModel);
		color =  ColorUtils.alphaToColor(alpha, textColor);

		final int width = linkController.getWidth(connectorModel);
		if (!isSourceVisible() || !isTargetVisible()) {
			stroke = new BasicStroke(width);
		}
		else{
			stroke = UITools.createStroke((float) width, linkController.getDashArray(connectorModel), BasicStroke.JOIN_ROUND);
		}
		showsControlPoints = false;
	}
	
	
	public boolean isShowsControlPoints() {
        return showsControlPoints;
    }


    public void setShowsControlPoints(boolean showsControlPoints) {
        this.showsControlPoints = showsControlPoints;
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

	private Rectangle drawEndPointText(final Graphics2D g, final String text, final Point endPoint, final Point controlPoint) {
		if (text == null || text.equals("")) {
			return null;
		}
		final TextPainter textPainter = new TextPainter(g, text);
		final int textWidth = textPainter.getTextWidth();
		final int textHeight = textPainter.getTextHeight();
		final int x;
		if (controlPoint.x > endPoint.x) {
			x = endPoint.x - textWidth - LABEL_GAP;
		}
		else {
			x = endPoint.x  + LABEL_GAP;
		}
		final int y;
		if (controlPoint.y > endPoint.y) {
			y = endPoint.y  + LABEL_GAP;
		}
		else {
			y = endPoint.y - textHeight - LABEL_GAP;
		}
		textPainter.draw(x, y, textColor, bgColor);
		return new Rectangle(x, y, textWidth, textHeight);
	}
	
	private Rectangle drawMiddleLabel(final Graphics2D g, final String text, final Point centerPoint) {
		if (text == null || text.equals("")) {
			return null;
		}
		final TextPainter textPainter = new TextPainter(g, text);
		final int textWidth = textPainter.getTextWidth();
		final int x = centerPoint.x - textWidth / 2;
		final int textHeight = textPainter.getTextHeight();
		int y = centerPoint.y - textHeight/2;
		textPainter.draw(x, y, textColor, bgColor);
		return new Rectangle(x, y, textWidth, textHeight);
	}

	Shape getArrowLinkCurve() {
		return arrowLinkCurve;
	}

	NodeLinkModel getArrowLinkModel() {
		return connectorModel;
	}

	private Point getCenterPoint() {
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
		boolean targetIsLeft = false;
		boolean sourceIsLeft = false;
		final Graphics2D g = (Graphics2D) graphics.create();
		final Color oldColor = g.getColor();
		g.setColor(color);
		/* set stroke. */
		g.setStroke(stroke);
        Point startInclination = connectorModel.getStartInclination();
        Point endInclination = connectorModel.getEndInclination();
		if (startInclination == null || endInclination == null) {
		    InclinationRecommender recommender = new InclinationRecommender(linkController, this);
			if (isSourceVisible() && startInclination == null) {
			    startInclination = recommender.calcStartInclination();
			}
			if (isTargetVisible() && connectorModel.getEndInclination() == null) {
			    endInclination = recommender.calcEndInclination();
			}
		}
		Point startPoint = null, endPoint = null;
        if (isSourceVisible()) {
            startPoint = source.getLinkPoint(startInclination);
            sourceIsLeft = source.isLeft();
        }
        if (isTargetVisible()) {
            endPoint = target.getLinkPoint(endInclination);
            targetIsLeft = target.isLeft();
        }
		final MapView map = getMap();
		Point startPoint2 = null;
		if (startPoint != null) {
			startPoint2 = new Point(startPoint);
			if(endPoint == null){
				normalizeLength(NORMAL_LENGTH, startInclination);
			}
			startPoint2.translate(((sourceIsLeft) ? -1 : 1) * map.getZoomed(startInclination.x),
				map.getZoomed(startInclination.y));

		}
		Point endPoint2 = null;
		if (endPoint != null) {
			endPoint2 = new Point(endPoint);
			if(startPoint == null){
				normalizeLength(NORMAL_LENGTH, endInclination);
			}
			endPoint2.translate(((targetIsLeft) ? -1 : 1) * map.getZoomed(endInclination.x), map
				.getZoomed(endInclination.y));
		}
		final boolean showsConnectors = map.showsConnectorLines();
		paintCurve(g, startPoint, startPoint2, endPoint2, endPoint, showsConnectors);
		if(showsConnectors) {
			drawLabels(g, startPoint, startPoint2, endPoint2, endPoint);
		}
		g.setColor(oldColor);
	}

	private void normalizeLength(int normalLength, Point startInclination) {
		double k = normalLength / Math.sqrt(startInclination.x * startInclination.x + startInclination.y * startInclination.y);
		startInclination.x *= k;
		startInclination.y *= k;
	}

	private Shape createLine(Point p1, Point p2) {
	    return new Line2D.Float(p1, p2);
    }

	private Shape createLinearPath(Point startPoint, Point startPoint2, Point endPoint2, Point endPoint) {
	    final GeneralPath generalPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, 4);
	    generalPath.moveTo(startPoint.x, startPoint.y);
	    generalPath.lineTo(startPoint2.x, startPoint2.y);
	    generalPath.lineTo(endPoint2.x, endPoint2.y);
	    generalPath.lineTo(endPoint.x, endPoint.y);
		return generalPath;
    }

	private void paintCurve(final Graphics2D g, Point startPoint, Point startPoint2, Point endPoint2, Point endPoint, boolean showsConnectors) {
		final boolean selfLink = getSource() == getTarget();
		final boolean isLine = ConnectorModel.Shape.LINE.equals(linkController.getShape(connectorModel));
		arrowLinkCurve = null;
		if (showsConnectors) {
		    if (startPoint != null && endPoint != null) {
		        if(isLine) {
		            if (selfLink) {
		                arrowLinkCurve = createLine(startPoint, startPoint2);
		            }
		            else {
		                arrowLinkCurve = createLine(startPoint, endPoint);
		            }
		        }
		        else if (ConnectorModel.Shape.LINEAR_PATH.equals(linkController.getShape(connectorModel)))
		            arrowLinkCurve = createLinearPath(startPoint, startPoint2, endPoint2, endPoint);
		        else
		            arrowLinkCurve = createCubicCurve2D(startPoint, startPoint2, endPoint2, endPoint);
		    } 
		    if (arrowLinkCurve != null) {
		        g.draw(arrowLinkCurve);
		    }
		} 
		if (isSourceVisible() && !(showsConnectors && linkController.getStartArrow(connectorModel).equals(ArrowType.NONE))) {
			if(!selfLink && isLine && endPoint != null)
				paintArrow(g, endPoint, startPoint);
			else
				paintArrow(g, startPoint2, startPoint);
		}
		if (isTargetVisible() && !(showsConnectors && linkController.getEndArrow(connectorModel).equals(ArrowType.NONE))) {
			if(isLine && startPoint != null) {
                            if (selfLink)
				paintArrow(g, startPoint, startPoint2);
                            else
				paintArrow(g, startPoint, endPoint);
                        }
			else
				paintArrow(g, endPoint2, endPoint);
		}
		if(showsConnectors) {
			if (showsControlPoints) {
				g.setColor(textColor);
				g.setStroke(new BasicStroke(stroke.getLineWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, DOTTED_DASH, 0));
			}
			if (showsControlPoints || !isSourceVisible() || !isTargetVisible()) {
				if (startPoint != null) {
					g.drawLine(startPoint.x, startPoint.y, startPoint2.x, startPoint2.y);
					drawCircle(g, startPoint2, source.getZoomedFoldingSymbolHalfWidth());
					if (arrowLinkCurve == null) {
						arrowLinkCurve = createLine(startPoint, startPoint2);
					}
				}
				if (endPoint != null && !(selfLink && isLine)) {
					g.drawLine(endPoint.x, endPoint.y, endPoint2.x, endPoint2.y);
					drawCircle(g, endPoint2, target.getZoomedFoldingSymbolHalfWidth());
					if (arrowLinkCurve == null) {
						arrowLinkCurve = createLine(endPoint, endPoint2);
					}
				}
			}
		}
    }

	private void drawCircle(Graphics2D g, Point p, int hw) {
		g.setStroke(DEF_STROKE);
		g.fillOval(p.x - hw, p.y - hw, hw*2, hw*2);
    }

	private void paintArrow(final Graphics2D g, Point from, Point to) {
	    paintArrow(from, to, g, getZoom() * 10);
    }

	private void drawLabels(final Graphics2D g, Point startPoint, Point startPoint2, Point endPoint2, Point endPoint) {
	    final String sourceLabel = connectorModel.getSourceLabel();
		final String middleLabel;
	      if(MapStyleModel.isDefaultStyleNode(source.getModel())) 
	          middleLabel = TextUtils.getText("connector");
	      else
	          middleLabel = connectorModel.getMiddleLabel();

		final String targetLabel = connectorModel.getTargetLabel();
		if (sourceLabel == null && middleLabel == null && targetLabel == null) {
			return;
		}

		final Font oldFont = g.getFont();
		final String fontFamily = linkController.getLabelFontFamily(connectorModel);
        final int fontSize = Math.round (linkController.getLabelFontSize(connectorModel) * UITools.FONT_SCALE_FACTOR);
        final Font linksFont = new Font(fontFamily, 0, getZoomed(fontSize));
        g.setFont(linksFont);

		if (startPoint != null) {
			sourceTextRectangle = drawEndPointText(g, sourceLabel, startPoint, startPoint2);
			if (endPoint == null) {
				middleTextRectangle = drawEndPointText(g, middleLabel, startPoint2, startPoint);
			}
		}
		if (endPoint != null) {
			targetTextRectangle = drawEndPointText(g, targetLabel, endPoint, endPoint2);
			if (startPoint == null) {
				middleTextRectangle = drawEndPointText(g, middleLabel, endPoint2, endPoint);
			}
		}
                if (startPoint != null && endPoint != null) {
                    middleTextRectangle = drawMiddleLabel(g, middleLabel, getCenterPoint());
		}
		g.setFont(oldFont);
    }

    private LinkController getLinkController() {
        return LinkController.getController(getModeController());
    }

	private CubicCurve2D createCubicCurve2D(Point startPoint, Point startPoint2, Point endPoint2, Point endPoint) {
	    final CubicCurve2D arrowLinkCurve = new CubicCurve2D.Double();
		if (startPoint != null && endPoint != null) {
			arrowLinkCurve.setCurve(startPoint, startPoint2, endPoint2, endPoint);
		}
		else if (startPoint != null) {
			arrowLinkCurve.setCurve(startPoint, startPoint2, startPoint, startPoint2);
		}
		else if (endPoint != null) {
			arrowLinkCurve.setCurve(endPoint, endPoint2, endPoint, endPoint2);
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
		increaseBounds(innerBounds, sourceTextRectangle);
		increaseBounds(innerBounds, middleTextRectangle);
		increaseBounds(innerBounds, targetTextRectangle);
	}

	private void increaseBounds(Rectangle innerBounds, Rectangle rect) {
	    if (rect != null)
                innerBounds.add(rect);
        }

}
