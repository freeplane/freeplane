package org.freeplane.features.treetable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.freeplane.features.map.NodeModel;

public class TreeTableSheetExport {
	private static class Row {
		private final NodeModel node;
		private final int depth;

		private Row(final NodeModel node, final int depth) {
			this.node = node;
			this.depth = depth;
		}
	}

	private final List<NodeModel> columnNodes;
	private final List<Row> rows;
	private final int treeColumnCount;
	private final int headerRowCount;
	private final TreeTableSheet sheet;

	public static boolean exportsAsSheet(final Collection<NodeModel> nodes) {
		return nodes.size() == 1 && TreeTableModel.isTreeTableRoot(nodes.iterator().next());
	}

	public static TreeTableSheet sheetOf(final NodeModel treeTableRoot) {
		return new TreeTableSheetExport(treeTableRoot).sheet;
	}

	private TreeTableSheetExport(final NodeModel treeTableRoot) {
		final NodeModel header = TreeTableHeader.headerOf(treeTableRoot);
		columnNodes = TreeTableHeader.distinctColumnNodesOf(treeTableRoot);
		rows = new ArrayList<Row>();
		collectRows(treeTableRoot, 0);
		treeColumnCount = maximalRowDepth() + 1;
		headerRowCount = header == null ? 1 : headerRowCountBelow(header);
		sheet = new TreeTableSheet(headerRowCount + rows.size(), treeColumnCount + columnNodes.size());
		sheet.set(0, 0, RichText.fromContent(treeTableRoot.getText()));
		sheet.span(0, 0, headerRowCount, treeColumnCount);
		if (header != null)
			placeHeaderChildrenOf(header, 0);
		placeRows();
	}

	private void collectRows(final NodeModel parent, final int depth) {
		for (final NodeModel child : parent.getChildren()) {
			if (TreeTableHeader.isHeaderRoot(child))
				continue;
			rows.add(new Row(child, depth));
			if (!TreeTableModel.isTreeTableRoot(child))
				collectRows(child, depth + 1);
		}
	}

	private int maximalRowDepth() {
		int depth = 0;
		for (final Row row : rows)
			depth = Math.max(depth, row.depth);
		return depth;
	}

	private int headerRowCountBelow(final NodeModel header) {
		int rowCount = 1;
		for (final NodeModel columnNode : columnNodes) {
			int level = 0;
			for (NodeModel ancestor = columnNode; ancestor != header; ancestor = ancestor.getParentNode())
				level++;
			rowCount = Math.max(rowCount, level);
		}
		return rowCount;
	}

	private void placeHeaderChildrenOf(final NodeModel parent, final int sheetRow) {
		for (final NodeModel child : parent.getChildren()) {
			final int firstColumn = firstColumnIndexUnder(child);
			if (firstColumn < 0)
				continue;
			final int sheetColumn = treeColumnCount + firstColumn;
			final boolean isColumn = !child.hasChildren();
			sheet.set(sheetRow, sheetColumn, TreeTableHeader.keyOf(child));
			sheet.span(sheetRow, sheetColumn, isColumn ? headerRowCount - sheetRow : 1,
				columnCountUnder(child, firstColumn));
			if (!isColumn)
				placeHeaderChildrenOf(child, sheetRow + 1);
		}
	}

	private int firstColumnIndexUnder(final NodeModel headerNode) {
		for (int index = 0; index < columnNodes.size(); index++) {
			if (isUnder(columnNodes.get(index), headerNode))
				return index;
		}
		return -1;
	}

	private int columnCountUnder(final NodeModel headerNode, final int firstColumn) {
		int count = 0;
		while (firstColumn + count < columnNodes.size() && isUnder(columnNodes.get(firstColumn + count), headerNode))
			count++;
		return count;
	}

	private static boolean isUnder(final NodeModel columnNode, final NodeModel headerNode) {
		return columnNode == headerNode || columnNode.isDescendantOf(headerNode);
	}

	private void placeRows() {
		for (int index = 0; index < rows.size(); index++) {
			final Row row = rows.get(index);
			final int sheetRow = headerRowCount + index;
			sheet.set(sheetRow, row.depth, RichText.fromContent(row.node.getText()));
			sheet.span(sheetRow, row.depth, 1, treeColumnCount - row.depth);
			for (int column = 0; column < columnNodes.size(); column++) {
				final String key = TreeTableHeader.keyOf(columnNodes.get(column));
				sheet.set(sheetRow, treeColumnCount + column,
					RichText.fromContent(TreeTableNodes.cellValueOf(row.node, key)));
			}
		}
	}
}
