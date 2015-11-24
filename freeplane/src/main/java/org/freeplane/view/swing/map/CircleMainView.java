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
	protected int getMinimumHorizontalInset() {
		return 3;
	}

	@Override
	protected int getMinimumVerticalInset() {
		return 3;
	}

	@Override
	public Dimension getPreferredSize() {
		if (isPreferredSizeSet()) {
			return super.getPreferredSize();
		}
		final Dimension prefSize = getPreferredSizeWithoutMargin(getMaximumWidth());
		int w = prefSize.width;
		int h = prefSize.height;
		int diameter = (int)(Math.ceil(Math.sqrt(w * w + h * h)));
		prefSize.width = (int) Math.ceil(Math.max(diameter, prefSize.width + getZoom() * getMinimumHorizontalInset()));
		prefSize.height = (int) Math.ceil(Math.max(diameter, prefSize.height + getZoom() * getMinimumVerticalInset()));
		if(prefSize.width < getMinimumWidth())
			prefSize.width = getMinimumWidth();
		if (prefSize.height < prefSize.width)
			prefSize.height = prefSize.width;
		else
			prefSize.width = prefSize.height;
		return prefSize;
	}
	
	@Override
	public Point getConnectorPoint(Point p) {
		return getConnectorPointAtTheOvalBorder(p);
	}
}
