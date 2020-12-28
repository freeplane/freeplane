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
import java.util.Optional;

import org.freeplane.features.link.ConnectorModel.Shape;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * 23.03.2014
 */
class ConnectorProperties{
	private Optional<Color> color;
	private Optional<Integer> alpha;
	private Optional<ArrowType> endArrow;
	private Optional<int[]> dash;
	private Optional<ArrowType> startArrow;
	private Optional<Integer> width;
	private Optional<Shape> shape;
	private Optional<String> labelFontFamily;
	private Optional<Integer> labelFontSize;

	private Point startInclination;
	private Point endInclination;
	
	private String sourceLabel;
	private String middleLabel;
	private String targetLabel;

    public ConnectorProperties() {
        this.startArrow = Optional.empty();
        this.endArrow = Optional.empty();
        this.dash = Optional.empty();
        this.color = Optional.empty();
        this.alpha = Optional.empty();
        this.width = Optional.empty();
        this.shape = Optional.empty();
        this.labelFontFamily = Optional.empty();
        this.labelFontSize = Optional.empty();
    }
	public ConnectorProperties(ConnectorArrows connectorEnds, int[] dash, final Color color,
	                      final int alpha, final Shape shape, final int width,
	                      final String labelFontFamily, final int labelFontSize) {
		assert color != null;
		assert shape != null;
		assert connectorEnds!=null;
		this.startArrow = Optional.of(connectorEnds.start);
		this.endArrow = Optional.of(connectorEnds.end);
		this.dash = Optional.of(dash);
		this.color = Optional.of(color);
		this.alpha = Optional.of(alpha);
		this.width = Optional.of(width);
		this.shape = Optional.of(shape);
		this.labelFontFamily = Optional.of(labelFontFamily);
		this.labelFontSize = Optional.of(labelFontSize);
	}
	public Optional<Shape> getShape() {
		return shape;
	}

	public void setShape(final Optional<Shape> shape) {
		this.shape = shape;
	}

	public Optional<int[]> getDash() {
		return dash;
	}

	public void setDash(Optional<int[]> dash) {
		this.dash = dash;
	}

	
	public Optional<Color> getColor() {
		return color;
	}

	public Optional<ArrowType> getEndArrow() {
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

	public Optional<ArrowType> getStartArrow() {
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

	public Optional<Integer>  getWidth() {
		return width;
	}

	public void setColor(final Optional<Color> color) {
		this.color = color;
	}

	public void setEndArrow(final Optional<ArrowType> endArrow) {
		this.endArrow = endArrow;
	}

	public void setEndInclination(final Point endInclination) {
		assert endInclination != null;
		this.endInclination = endInclination;
	}

	public void setMiddleLabel(final String middleLabel) {
		this.middleLabel = empty2null(middleLabel);
	}

	public void setSourceLabel(final String label) {
		sourceLabel = empty2null(label);
	}

	public void setStartArrow(final Optional<ArrowType> startArrow) {
		this.startArrow = startArrow;
	}

	public void setStartInclination(final Point startInclination) {
		this.startInclination = startInclination;
	}

	public void setTargetLabel(final String targetLabel) {
		this.targetLabel = empty2null(targetLabel);
	}

	public void setWidth(final Optional<Integer> width) {
		this.width = width;
	}

	public void setAlpha(Optional<Integer> alpha) {
	    this.alpha = alpha;
    }

	public Optional<Integer> getAlpha() {
	    return alpha;
    }
	public Optional<String> getLabelFontFamily() {
    	return labelFontFamily;
    }

	public void setLabelFontFamily(Optional<String> labelFontFamily) {
    	this.labelFontFamily = labelFontFamily;
    }

	public Optional<Integer>  getLabelFontSize() {
    	return labelFontSize;
    }

	public void setLabelFontSize(Optional<Integer>  labelFontSize) {
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