package org.freeplane.view.swing.map;

import java.awt.Rectangle;

class TreeTableHeaderButton {
	static final int SIZE = 16;
	static final String LABEL = "...";

	static Rectangle boundsIn(final Rectangle headerBounds, final int size) {
		final int width = Math.min(size, headerBounds.width);
		final int height = Math.min(size, headerBounds.height);
		return new Rectangle(headerBounds.x + headerBounds.width - width,
			headerBounds.y + (headerBounds.height - height) / 2, width, height);
	}

	static Rectangle textBoundsIn(final Rectangle headerBounds, final int size) {
		final int buttonWidth = Math.min(size, headerBounds.width);
		return new Rectangle(headerBounds.x, headerBounds.y, headerBounds.width - buttonWidth, headerBounds.height);
	}

	private TreeTableHeaderButton() {
	}
}
