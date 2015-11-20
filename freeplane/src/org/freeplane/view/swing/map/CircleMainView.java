package org.freeplane.view.swing.map;

import java.awt.Dimension;

public class CircleMainView extends OvalMainView {

	public CircleMainView(String shape) {
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
