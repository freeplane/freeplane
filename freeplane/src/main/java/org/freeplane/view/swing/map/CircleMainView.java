package org.freeplane.view.swing.map;

import java.awt.Dimension;
import java.awt.Point;

import org.freeplane.features.nodestyle.NodeStyleModel.Shape;

@SuppressWarnings("serial")
public class CircleMainView extends OvalMainView {
	public CircleMainView() {
		super();
	}
	
	@Override
    public
    Shape getShape() {
		return Shape.circle;
	}
	
	@Override
	public Dimension getPreferredSize() {
		final Dimension prefSize = super.getPreferredSize();
		if (isPreferredSizeSet()) {
			return prefSize;
		}
		if (prefSize.height <= prefSize.width)
			prefSize.height = prefSize.width;
		else {
			prefSize.width = Math.min(getMaximumWidth(), prefSize.height);
		}
		return prefSize;
	}
	
	@Override
	public Point getConnectorPoint(Point p) {
		return getConnectorPointAtTheOvalBorder(p);
	}
}
