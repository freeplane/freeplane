package org.freeplane.features.treetable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.StyleFactory;

public class TreeTableHeader {
	public static final String STYLE_NAME = "TreeHeader";

	public static IStyle style() {
		return StyleFactory.create(STYLE_NAME);
	}

	public static boolean isHeaderRoot(final NodeModel node) {
		final NodeModel parent = node.getParentNode();
		return parent != null && TreeTableModel.isTreeTableRoot(parent)
				&& style().equals(LogicalStyleModel.getStyle(node));
	}

	public static NodeModel headerOf(final NodeModel treeTableRoot) {
		for (final NodeModel child : treeTableRoot.getChildren()) {
			if (isHeaderRoot(child))
				return child;
		}
		return null;
	}

	public static NodeModel headerRootAbove(final NodeModel node) {
		for (NodeModel ancestor = node; ancestor != null; ancestor = ancestor.getParentNode()) {
			if (isHeaderRoot(ancestor))
				return ancestor;
			if (TreeTableModel.isTreeTableRoot(ancestor))
				return null;
		}
		return null;
	}

	public static boolean isInHeader(final NodeModel node) {
		return headerRootAbove(node) != null;
	}

	public static boolean isBelowHeaderRoot(final NodeModel node) {
		return isInHeader(node) && !isHeaderRoot(node);
	}

	public static boolean isColumnNode(final NodeModel node) {
		return isBelowHeaderRoot(node) && !node.hasChildren();
	}

	public static List<NodeModel> columnNodesOf(final NodeModel header) {
		final List<NodeModel> columnNodes = new ArrayList<NodeModel>();
		for (final NodeModel child : header.getChildren())
			collectColumnNodes(child, columnNodes);
		return columnNodes;
	}

	private static void collectColumnNodes(final NodeModel node, final List<NodeModel> columnNodes) {
		if (!node.hasChildren())
			columnNodes.add(node);
		for (final NodeModel child : node.getChildren())
			collectColumnNodes(child, columnNodes);
	}

	public static String keyOf(final NodeModel columnNode) {
		return keyOfText(columnNode.getText());
	}

	public static String keyOfText(final Object text) {
		return text == null ? "" : HtmlUtils.htmlToPlain(text.toString()).trim();
	}

	public static List<NodeModel> distinctColumnNodes(final List<NodeModel> columnNodes) {
		final List<NodeModel> distinct = new ArrayList<NodeModel>();
		final Set<String> keys = new HashSet<String>();
		for (final NodeModel columnNode : columnNodes) {
			final String key = keyOf(columnNode);
			if (!key.isEmpty() && keys.add(key))
				distinct.add(columnNode);
		}
		return distinct;
	}

	public static List<NodeModel> distinctColumnNodesOf(final NodeModel treeTableRoot) {
		final NodeModel header = headerOf(treeTableRoot);
		return header == null ? new ArrayList<NodeModel>() : distinctColumnNodes(columnNodesOf(header));
	}

	public static TreeTableColumns columnsOf(final NodeModel treeTableRoot) {
		final List<NodeModel> columnNodes = distinctColumnNodesOf(treeTableRoot);
		final TreeTableColumn[] columns = new TreeTableColumn[columnNodes.size()];
		for (int index = 0; index < columns.length; index++)
			columns[index] = new TreeTableColumn(keyOf(columnNodes.get(index)));
		return TreeTableColumns.of(columns);
	}

	public static boolean canRemoveColumn(final NodeModel columnNode) {
		final NodeModel header = headerRootAbove(columnNode);
		return isColumnNode(columnNode) && columnNodesOf(header).size() > 1;
	}

	public static boolean canMoveColumnLeft(final NodeModel columnNode) {
		return isBelowHeaderRoot(columnNode) && columnNode.getParentNode().getIndex(columnNode) > 0;
	}

	public static boolean canMoveColumnRight(final NodeModel columnNode) {
		if (!isBelowHeaderRoot(columnNode))
			return false;
		final NodeModel parent = columnNode.getParentNode();
		return parent.getIndex(columnNode) < parent.getChildCount() - 1;
	}

	public static boolean acceptsRenaming(final NodeModel columnNode, final String newKey) {
		final NodeModel treeTableRoot = TreeTableNodes.treeTableRootOf(columnNode);
		return isColumnNode(columnNode) && treeTableRoot != null
				&& columnsOf(treeTableRoot).acceptsRenaming(keyOf(columnNode), newKey == null ? null : newKey.trim());
	}

	public static NodeModel lowestCommonAncestor(final NodeModel first, final NodeModel second) {
		for (NodeModel ancestor = first; ancestor != null; ancestor = ancestor.getParentNode()) {
			if (ancestor == second || second.isDescendantOf(ancestor))
				return ancestor;
		}
		return null;
	}

	private TreeTableHeader() {
	}
}
