package org.freeplane.view.swing.map;

import java.awt.Dimension;
import java.awt.Point;

import org.freeplane.features.nodestyle.NodeStyleModel.Shape;

public class BigOvalMainView extends OvalMainView {

	public BigOvalMainView() {
		super();
	}
	
	@Override
    public
    Shape getShape() {
		return Shape.big_oval;
	}
	
	@Override
	public Dimension getPreferredSize(int minimumWidth, int maximumWidth) {
		final Dimension prefSize = super.getPreferredSize(minimumWidth, maximumWidth);
		if (isPreferredSizeSet()) {
			return prefSize;
		}
		prefSize.height *= 2;
		return prefSize;
	}

	protected double insetsScalingFactor() {
		return 0.1;
	}

	@Override
	public Point getConnectorPoint(Point p) {
		return USE_COMMON_OUT_POINT_FOR_ROOT_NODE || ! getNodeView().isRoot() ? 
				super.getConnectorPoint(p) : getConnectorPointAtTheOvalBorder(p);
	}
}
