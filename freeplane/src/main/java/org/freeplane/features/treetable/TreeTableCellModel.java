package org.freeplane.features.treetable;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

public class TreeTableCellModel implements IExtension {
	private static final TreeTableCellModel INSTANCE = new TreeTableCellModel();

	public static void markAsCell(final NodeModel node) {
		node.addExtension(INSTANCE);
	}

	public static boolean isMarkedAsCell(final NodeModel node) {
		return node.getExtension(TreeTableCellModel.class) != null;
	}

	private TreeTableCellModel() {
	}
}
