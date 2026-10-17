package org.freeplane.view.swing.map;

import java.awt.Rectangle;

import org.freeplane.features.map.NodeModel;

class TreeTableCell {
	private final NodeModel node;
	private final String attributeKey;
	private final Rectangle bounds;
	private final boolean header;

	static TreeTableCell header(final NodeModel treeTableRoot, final String attributeKey, final Rectangle bounds) {
		return new TreeTableCell(treeTableRoot, attributeKey, bounds, true);
	}

	static TreeTableCell value(final NodeModel row, final String attributeKey, final Rectangle bounds) {
		return new TreeTableCell(row, attributeKey, bounds, false);
	}

	private TreeTableCell(final NodeModel node, final String attributeKey, final Rectangle bounds,
			final boolean header) {
		this.node = node;
		this.attributeKey = attributeKey;
		this.bounds = bounds;
		this.header = header;
	}

	NodeModel getNode() {
		return node;
	}

	String getAttributeKey() {
		return attributeKey;
	}

	Rectangle getBounds() {
		return new Rectangle(bounds);
	}

	boolean isHeader() {
		return header;
	}
}
