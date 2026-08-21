package org.freeplane.features.treetable;

import org.freeplane.features.map.NodeModel;

public class TreeTableNodes {

	public static NodeModel treeTableRootOf(final NodeModel node) {
		for (NodeModel ancestor = node.getParentNode(); ancestor != null; ancestor = ancestor.getParentNode()) {
			if (TreeTableModel.isTreeTableRoot(ancestor))
				return ancestor;
		}
		return null;
	}

	public static boolean isRow(final NodeModel node) {
		return treeTableRootOf(node) != null;
	}

	public static int rowDepth(final NodeModel node) {
		int depth = 0;
		for (NodeModel ancestor = node.getParentNode(); ancestor != null; ancestor = ancestor.getParentNode()) {
			depth++;
			if (TreeTableModel.isTreeTableRoot(ancestor))
				return depth;
		}
		return -1;
	}

	public static TreeTableColumns columnsFor(final NodeModel node) {
		final NodeModel treeTableRoot = treeTableRootOf(node);
		return treeTableRoot != null ? TreeTableModel.columnsOf(treeTableRoot) : TreeTableColumns.empty();
	}

	private TreeTableNodes() {
	}
}
