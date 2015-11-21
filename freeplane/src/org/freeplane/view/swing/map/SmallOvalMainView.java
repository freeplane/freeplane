package org.freeplane.view.swing.map;

import java.awt.Point;

import org.freeplane.features.nodestyle.NodeStyleModel.Shape;

@SuppressWarnings("serial")
public class SmallOvalMainView extends OvalMainView {

	public SmallOvalMainView() {
		super();
	}
	
	@Override
    public
    Shape getShape() {
		return Shape.small_oval;
	}

	protected double insetsScalingFactor() {
		return 0.4;
	}

	@Override
	public Point getConnectorPoint(Point p) {
		return USE_COMMON_OUT_POINT_FOR_ROOT_NODE || ! getNodeView().isRoot() ? 
				super.getConnectorPoint(p) : getConnectorPointAtTheOvalBorder(p);
	}
	
}
