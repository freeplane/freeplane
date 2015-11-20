package org.freeplane.view.swing.map;

import java.awt.Dimension;

import org.freeplane.features.nodestyle.NodeStyleModel.Shape;

public class HighOvalMainView extends OvalMainView {

	public HighOvalMainView(Shape shape) {
		super(shape);
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
}
