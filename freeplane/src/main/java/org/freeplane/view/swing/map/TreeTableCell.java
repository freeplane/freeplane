package org.freeplane.view.swing.map;

import java.awt.Rectangle;

import org.freeplane.features.map.NodeModel;

class TreeTableCell {
	private final NodeModel row;
	private final String attributeKey;
	private final Rectangle bounds;

	TreeTableCell(final NodeModel row, final String attributeKey, final Rectangle bounds) {
		this.row = row;
		this.attributeKey = attributeKey;
		this.bounds = bounds;
	}

	NodeModel getRow() {
		return row;
	}

	String getAttributeKey() {
		return attributeKey;
	}

	Rectangle getBounds() {
		return new Rectangle(bounds);
	}
}
