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
package org.freeplane.map.link;

import java.awt.Color;
import java.awt.Point;

import org.freeplane.core.map.NodeModel;
import org.freeplane.view.map.MapView;
import org.freeplane.view.map.NodeView;

public class ArrowLinkModel extends LinkModel {
	private Color color;
	private String endArrow;
	private Point endInclination;
	private String referenceText;
	private boolean showControlPointsFlag;
	private NodeModel source;
	private String startArrow;
	private Point startInclination;
	private String style;
	private int width;

	public ArrowLinkModel(final NodeModel source, final String targetID) {
		super(targetID);
		this.source = source;
		referenceText = null;
		startArrow = "None";
		endArrow = "Default";
	}

	private void changeInclination(int deltaX, final int deltaY, final NodeView linkedNodeView,
	                               final Point changedInclination) {
		if (linkedNodeView.isLeft()) {
			deltaX = -deltaX;
		}
		changedInclination.translate(deltaX, deltaY);
		if (changedInclination.x != 0
		        && Math.abs((double) changedInclination.y / changedInclination.x) < 0.015) {
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

	/*
	 * (non-Javadoc)
	 * @see freemind.modes.MindMapArrowLink#changeInclination(int, int, int,
	 * int)
	 */
	public void changeInclination(final MapView map, final int originX, final int originY,
	                              final int deltaX, final int deltaY) {
		double distSqToTarget = 0;
		double distSqToSource = 0;
		final NodeView targetView = map.getNodeView(getTarget());
		final NodeView sourceView = map.getNodeView(getSource());
		if (targetView != null && sourceView != null) {
			final Point targetLinkPoint = targetView.getLinkPoint(getEndInclination());
			final Point sourceLinkPoint = sourceView.getLinkPoint(getStartInclination());
			distSqToTarget = targetLinkPoint.distanceSq(originX, originY);
			distSqToSource = sourceLinkPoint.distanceSq(originX, originY);
		}
		if ((targetView == null || sourceView != null) && distSqToSource < distSqToTarget * 2.25) {
			final Point changedInclination = getStartInclination();
			changeInclination(deltaX, deltaY, sourceView, changedInclination);
			setStartInclination(changedInclination);
		}
		if ((sourceView == null || targetView != null) && distSqToTarget < distSqToSource * 2.25) {
			final Point changedInclination = getEndInclination();
			changeInclination(deltaX, deltaY, targetView, changedInclination);
			setEndInclination(changedInclination);
		}
	}

	public Color getColor() {
		return color;
	}

	public String getEndArrow() {
		return endArrow;
	}

	public Point getEndInclination() {
		if (endInclination == null) {
			return null;
		}
		return new Point(endInclination);
	}

	public String getReferenceText() {
		return referenceText;
	}

	public boolean getShowControlPointsFlag() {
		return showControlPointsFlag;
	}

	public NodeModel getSource() {
		return source;
	}

	public String getStartArrow() {
		return startArrow;
	}

	public Point getStartInclination() {
		if (startInclination == null) {
			return null;
		}
		return new Point(startInclination);
	}

	public String getStyle() {
		return style;
	}

	public NodeModel getTarget() {
		return source.getMap().getNodeForID(getTargetID());
	}

	public int getWidth() {
		return width;
	}

	public void setColor(final Color color) {
		this.color = color;
	}

	public void setEndArrow(final String endArrow) {
		if (endArrow == null || endArrow.toUpperCase().equals("NONE")) {
			this.endArrow = "None";
			return;
		}
		else if (endArrow.toUpperCase().equals("DEFAULT")) {
			this.endArrow = "Default";
			return;
		}
		System.err.println("Cannot set the end arrow type to " + endArrow);
	}

	public void setEndInclination(final Point endInclination) {
		this.endInclination = endInclination;
	}

	public void setReferenceText(final String referenceText) {
		this.referenceText = referenceText;
	}

	public void setShowControlPoints(final boolean bShowControlPointsFlag) {
		showControlPointsFlag = bShowControlPointsFlag;
	}

	public void setSource(final NodeModel source) {
		this.source = source;
	}

	public void setStartArrow(final String startArrow) {
		if (startArrow == null || startArrow.toUpperCase().equals("NONE")) {
			this.startArrow = "None";
			return;
		}
		else if (startArrow.toUpperCase().equals("DEFAULT")) {
			this.startArrow = "Default";
			return;
		}
		System.err.println("Cannot set the start arrow type to " + startArrow);
	}

	public void setStartInclination(final Point startInclination) {
		this.startInclination = startInclination;
	}

	public void setStyle(final String style) {
		this.style = style;
	}

	public void setWidth(final int width) {
		this.width = width;
	}
}
