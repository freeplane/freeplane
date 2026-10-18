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
		return treeTableRootOf(node) != null && !TreeTableHeader.isInHeader(node);
	}

	public static List<NodeModel> rowsOf(final NodeModel treeTableRoot) {
		final List<NodeModel> rows = new ArrayList<NodeModel>();
		collectRows(treeTableRoot, rows);
		return rows;
	}

	private static void collectRows(final NodeModel parent, final List<NodeModel> rows) {
		for (final NodeModel child : parent.getChildren()) {
			if (TreeTableHeader.isHeaderRoot(child))
				continue;
			rows.add(child);
			if (!TreeTableModel.isTreeTableRoot(child))
				collectRows(child, rows);
		}
	}

	private TreeTableNodes() {
	}
}
