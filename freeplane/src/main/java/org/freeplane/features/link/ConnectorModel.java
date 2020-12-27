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
package org.freeplane.features.link;

import java.awt.Color;
import java.awt.Point;
import java.util.Optional;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeRelativePath;

public class ConnectorModel extends NodeLinkModel {

	public static enum Shape {
		LINE, LINEAR_PATH, CUBIC_CURVE, EDGE_LIKE
	};
	
	final private ConnectorProperties connectorProperties;
	
	public ConnectorModel(NodeModel source, String targetID) {
	    this(source, targetID, new ConnectorProperties());
	}
	public ConnectorModel(final NodeModel source, final String targetID,
			final ConnectorArrows connectorEnds, int[] dash, 
			final Color color,final int alpha, final Shape shape, final int width,
	                      final String labelFontFamily, final int labelFontSize) {
		this(source, targetID, new ConnectorProperties(connectorEnds, dash, color, alpha, shape, width, labelFontFamily, labelFontSize));
	}

	private ConnectorModel(final NodeModel source, final String targetID, final ConnectorProperties connectorProperties) {
		super(source, targetID);
		assert source != null;
		this.connectorProperties = connectorProperties;
	}


    public Optional<Shape> getShape() {
	    return connectorProperties.getShape();
    }

	public void setShape( Optional<Shape> shape) {
	    connectorProperties.setShape(shape);
    }

	public  Optional<int[]> getDash() {
	    return connectorProperties.getDash();
    }

	public void setDash(Optional<int[]> dash) {
	    connectorProperties.setDash(dash);
    }

	public Optional<Color> getColor() {
	    return connectorProperties.getColor();
    }

	public Optional<ArrowType> getEndArrow() {
	    return connectorProperties.getEndArrow();
    }

	public Point getEndInclination() {
	    return connectorProperties.getEndInclination();
    }

	public String getMiddleLabel() {
	    return connectorProperties.getMiddleLabel();
    }

	public String getSourceLabel() {
	    return connectorProperties.getSourceLabel();
    }

	public Optional<ArrowType> getStartArrow() {
	    return connectorProperties.getStartArrow();
    }

	public Point getStartInclination() {
	    return connectorProperties.getStartInclination();
    }

	public String getTargetLabel() {
	    return connectorProperties.getTargetLabel();
    }

	public Optional<Integer> getWidth() {
	    return connectorProperties.getWidth();
    }

	public void setColor(Optional<Color> color) {
	    connectorProperties.setColor(color);
    }

	public void setEndArrow(Optional<ArrowType> endArrow) {
	    connectorProperties.setEndArrow(endArrow);
    }

	public void setEndInclination(Point endInclination) {
	    connectorProperties.setEndInclination(endInclination);
    }

	public void setMiddleLabel(String middleLabel) {
	    connectorProperties.setMiddleLabel(middleLabel);
    }

	public boolean getShowControlPointsFlag() {
	    return connectorProperties.getShowControlPointsFlag();
    }

	public void setShowControlPoints(boolean bShowControlPointsFlag) {
	    connectorProperties.setShowControlPoints(bShowControlPointsFlag);
    }

	public void setSourceLabel(String label) {
	    connectorProperties.setSourceLabel(label);
    }

	public void setStartArrow(Optional<ArrowType> startArrow) {
	    connectorProperties.setStartArrow(startArrow);
    }

	public void setStartInclination(Point startInclination) {
	    connectorProperties.setStartInclination(startInclination);
    }

	public void setTargetLabel(String targetLabel) {
	    connectorProperties.setTargetLabel(targetLabel);
    }

	public void setWidth(Optional<Integer> width) {
	    connectorProperties.setWidth(width);
    }

	public void setAlpha(Optional<Integer> alpha) {
	    connectorProperties.setAlpha(alpha);
    }

	public Optional<Integer> getAlpha() {
	    return connectorProperties.getAlpha();
    }

	public Optional<String> getLabelFontFamily() {
	    return connectorProperties.getLabelFontFamily();
    }

	public void setLabelFontFamily(Optional<String> labelFontFamily) {
	    connectorProperties.setLabelFontFamily(labelFontFamily);
    }

	public Optional<Integer> getLabelFontSize() {
	    return connectorProperties.getLabelFontSize();
    }

	public void setLabelFontSize(Optional<Integer> labelFontSize) {
	    connectorProperties.setLabelFontSize(labelFontSize);
    }

	public void changeInclination(int deltaX, int deltaY, NodeModel linkedNodeView, Point changedInclination) {
	    connectorProperties.changeInclination(deltaX, deltaY, linkedNodeView, changedInclination);
    }

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + connectorProperties.hashCode();
	    result = prime * result + getSource().hashCode();
	    final String targetID = getTargetID();
	    if(targetID == null)
	    	return result;
		result = prime * result + targetID.hashCode();
		return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    ConnectorModel other = (ConnectorModel) obj;
	    if (!connectorProperties.equals(other.connectorProperties) || !getSource().equals(other.getSource()))
	        return false;
	    final String targetID = getTargetID();
	    if(targetID == null)
	    	return other.getTargetID() == null;
	    else
	    	return targetID.equals(other.getTargetID());
    }

	public NodeLinkModel cloneForSource(NodeModel sourceClone, String targetId) {
	    return new ConnectorModel(sourceClone, targetId, connectorProperties);
    }

	public NodeLinkModel cloneForSource(NodeModel sourceClone) {
		final NodeModel source = getSource();
		if(sourceClone == source)
			return this;
		if(sourceClone.getMap().getNodeForID(source.getID()) == null)
			return null;
		final NodeModel target = getTarget();
		if(target != null && target.getParentNode() != null){
			final NodeRelativePath nodeRelativePath = new NodeRelativePath(source, target);
			final NodeModel commonAncestor = nodeRelativePath.commonAncestor();
			final NodeModel ancestorClone = nodeRelativePath.ancestorForBegin(sourceClone);
			if(commonAncestor.isSubtreeCloneOf(ancestorClone)) {
	            final NodeRelativePath pathAncestorToSource = new NodeRelativePath(commonAncestor, source);
				final NodeRelativePath clonePath = new NodeRelativePath(ancestorClone, sourceClone);
				if (pathAncestorToSource.equalPathsTo(clonePath)) {
	            	final NodeModel targetClone = nodeRelativePath.pathEnd(ancestorClone);
	            	String targetID = targetClone.createID();
	            	return cloneForSource(sourceClone, targetID);
	            }
	        }
		}
		return null;
	}
}
