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
package org.freeplane.features.common.link;

import java.awt.Color;
import java.awt.Point;

import org.freeplane.core.model.NodeModel;

public class ConnectorModel extends NodeLinkModel {
	private Color color;
	private ArrowType endArrow;
	private Point endInclination;
	private String middleLabel;
	private boolean showControlPointsFlag;
	private String sourceLabel;
	private ArrowType startArrow;
	private Point startInclination;
	private String targetLabel;
	private int width;
	private boolean edgeLike;

	public boolean isEdgeLike() {
    	return edgeLike;
    }

	public void setEdgeLike(boolean edgeLike) {
    	this.edgeLike = edgeLike;
    }

	public ConnectorModel(final NodeModel source, final String targetID) {
		super(source, targetID);
		startArrow = ArrowType.NONE;
		endArrow = ArrowType.DEFAULT;
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

	private String empty2null(final String label) {
		return "".equals(label) ? null : label;
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

	public boolean getShowControlPointsFlag() {
		return showControlPointsFlag;
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
		this.color = color;
	}

	public void setEndArrow(final ArrowType endArrow) {
		this.endArrow = endArrow;
	}

	public void setEndInclination(final Point endInclination) {
		this.endInclination = endInclination;
	}

	public void setMiddleLabel(final String middleLabel) {
		this.middleLabel = empty2null(middleLabel);
	}

	public void setShowControlPoints(final boolean bShowControlPointsFlag) {
		showControlPointsFlag = bShowControlPointsFlag;
	}

	public void setSourceLabel(final String label) {
		sourceLabel = empty2null(label);
	}

	public void setStartArrow(final ArrowType startArrow) {
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
}
