package org.freeplane.features.treetable;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.features.map.NodeModel;

public class TreeTableNodes {

	public static NodeModel treeTableRootOf(final NodeModel node) {
		for (NodeModel ancestor = node.getParentNode(); ancestor != null; ancestor = ancestor.getParentNode()) {
			if (TreeTableModel.isTreeTableRoot(ancestor))
				return ancestor;
		}
		return null;
	}

	public static NodeModel enclosingTreeTableRoot(final NodeModel node) {
		return TreeTableModel.isTreeTableRoot(node) ? node : treeTableRootOf(node);
	}

	public static boolean canConvertToTable(final NodeModel node) {
		return node != null && enclosingTreeTableRoot(node) == null;
	}

	public static boolean canDeleteTable(final NodeModel node) {
		return node != null && TreeTableModel.isTreeTableRoot(node);
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

	public static List<NodeModel> rowsOf(final NodeModel treeTableRoot) {
		final List<NodeModel> rows = new ArrayList<NodeModel>();
		collectRows(treeTableRoot, rows);
		return rows;
	}

	private static void collectRows(final NodeModel parent, final List<NodeModel> rows) {
		for (final NodeModel child : parent.getChildren()) {
			rows.add(child);
			if (!TreeTableModel.isTreeTableRoot(child))
				collectRows(child, rows);
		}
	}

	public static TreeTableColumns columnsFor(final NodeModel node) {
		final NodeModel treeTableRoot = treeTableRootOf(node);
		return treeTableRoot != null ? TreeTableModel.columnsOf(treeTableRoot) : TreeTableColumns.empty();
	}

	private TreeTableNodes() {
	}
}
