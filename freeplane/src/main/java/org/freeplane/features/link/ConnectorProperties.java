/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 Dimitry
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
package org.freeplane.features.link;

import java.awt.Color;
import java.awt.Point;

import org.freeplane.features.link.ConnectorModel.Shape;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * 23.03.2014
 */
class ConnectorProperties{
	private Color color;
	private int alpha;
	private ArrowType endArrow;
	private int[] dash;
	private Point endInclination;
	private String middleLabel;
	private String sourceLabel;
	private ArrowType startArrow;
	private Point startInclination;
	private String targetLabel;
	private int width;
	private Shape shape;
	
	private String labelFontFamily;
	private int labelFontSize;

	public ConnectorProperties(ConnectorArrows connectorEnds, int[] dash, final Color color,
	                      final int alpha, final Shape shape, final int width,
	                      final String labelFontFamily, final int labelFontSize) {
		assert color != null;
		assert shape != null;
		assert connectorEnds!=null;
		this.startArrow = connectorEnds.start;
		this.endArrow = connectorEnds.end;
		this.dash = dash;
		this.color = color;
		this.setAlpha(alpha);
		this.width = width;
		this.shape = shape;
		this.labelFontFamily = labelFontFamily;
		this.labelFontSize = labelFontSize;
	}
	public Shape getShape() {
		return shape;
	}

	public void setShape(final Shape shape) {
		assert shape != null;
		this.shape = shape;
	}

	public int[] getDash() {
		return dash;
	}

	public void setDash(int[] dash) {
		this.dash = dash;
	}

	
	public Color getColor() {
		return color;
	}

	public ArrowType getEndArrow() {
		return endArrow;
	}

	public Point getEndInclination() {
		if (endInclination == null) {
			return null;
		}
		return new Point(endInclination);
	}

	public String getMiddleLabel() {
		return middleLabel;
	}

	public String getSourceLabel() {
		return sourceLabel;
	}

	public ArrowType getStartArrow() {
		return startArrow;
	}

	public Point getStartInclination() {
		if (startInclination == null) {
			return null;
		}
		return new Point(startInclination);
	}

	public String getTargetLabel() {
		return targetLabel;
	}

	public int getWidth() {
		return width;
	}

	public void setColor(final Color color) {
		assert color != null;
		this.color = color;
	}

	public void setEndArrow(final ArrowType endArrow) {
		assert endArrow != null;
		this.endArrow = endArrow;
	}

	public void setEndInclination(final Point endInclination) {
		assert endInclination != null;
		this.endInclination = endInclination;
	}

	public void setMiddleLabel(final String middleLabel) {
		this.middleLabel = empty2null(middleLabel);
	}

	private boolean showControlPointsFlag;

	public boolean getShowControlPointsFlag() {
		return showControlPointsFlag;
	}

	public void setShowControlPoints(final boolean bShowControlPointsFlag) {
		showControlPointsFlag = bShowControlPointsFlag;
	}

	public void setSourceLabel(final String label) {
		sourceLabel = empty2null(label);
	}

	public void setStartArrow(final ArrowType startArrow) {
		assert startArrow != null;
		this.startArrow = startArrow;
	}

	public void setStartInclination(final Point startInclination) {
		this.startInclination = startInclination;
	}

	public void setTargetLabel(final String targetLabel) {
		this.targetLabel = empty2null(targetLabel);
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public void setAlpha(int alpha) {
	    this.alpha = alpha;
    }

	public int getAlpha() {
	    return alpha;
    }
	public String getLabelFontFamily() {
    	return labelFontFamily;
    }

	public void setLabelFontFamily(String labelFontFamily) {
    	this.labelFontFamily = labelFontFamily;
    }

	public int getLabelFontSize() {
    	return labelFontSize;
    }

	public void setLabelFontSize(int labelFontSize) {
    	this.labelFontSize = labelFontSize;
    }

	private String empty2null(final String label) {
		return "".equals(label) ? null : label;
	}

	public void changeInclination(int deltaX, final int deltaY, final NodeModel linkedNodeView,
	                              final Point changedInclination) {
		if (linkedNodeView.isLeft()) {
			deltaX = -deltaX;
		}
		changedInclination.translate(deltaX, deltaY);
		if (changedInclination.x != 0 && Math.abs((double) changedInclination.y / changedInclination.x) < 0.015) {
			changedInclination.y = 0;
		}
		final double k = changedInclination.distance(0, 0);
		if (k < 10) {
			if (k > 0) {
				changedInclination.x = (int) (changedInclination.x * 10 / k);
				changedInclination.y = (int) (changedInclination.y * 10 / k);
			}
			else {
				changedInclination.x = 10;
			}
		}
	}

}