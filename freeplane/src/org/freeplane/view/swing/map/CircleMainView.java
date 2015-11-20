package org.freeplane.view.swing.map;

import java.awt.Dimension;

import org.freeplane.features.nodestyle.NodeStyleModel.Shape;

public class CircleMainView extends OvalMainView {

	public CircleMainView(Shape shape) {
		super(shape);
	}
	
	@Override
	public Dimension getPreferredSize(int minimumWidth, int maximumWidth) {
		final Dimension prefSize = super.getPreferredSize(minimumWidth, maximumWidth);
		if (isPreferredSizeSet()) {
			return prefSize;
		}
		if (prefSize.height <= prefSize.width)
			prefSize.height = prefSize.width;
		else {
			prefSize.width = Math.min(maximumWidth, prefSize.height);
		}
		return prefSize;
	}
}
