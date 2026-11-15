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
	private static final String UNNAMED_COLUMN_KEY_PREFIX = "#";
	private static final String PATH_SEPARATOR_CHARACTER = "\u203A";
	private static final String PATH_SEPARATOR = " " + PATH_SEPARATOR_CHARACTER + " ";

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

	public static String keyOf(final NodeModel node) {
		return keyOf(node, node, node.getText());
	}

	public static String keyOf(final NodeModel node, final Object text) {
		return keyOf(node, node, text);
	}

	public static String keyOf(final NodeModel node, final NodeModel renamedNode, final Object renamedText) {
		if (!isBelowHeaderRoot(node))
			return keyOfText(node == renamedNode ? renamedText : node.getText());
		String key = null;
		for (NodeModel pathNode = node; pathNode != null && !isHeaderRoot(pathNode); pathNode = pathNode.getParentNode())
			key = key == null ? segmentOf(pathNode, pathNode == renamedNode ? renamedText : pathNode.getText())
				: pathKey(segmentOf(pathNode, pathNode == renamedNode ? renamedText : pathNode.getText()), key);
		return key;
	}

	public static String pathKey(final String parentKey, final String segment) {
		return parentKey.isEmpty() ? segment : parentKey + PATH_SEPARATOR + segment;
	}

	public static String segmentOf(final NodeModel columnNode, final Object text) {
		final String name = keyOfText(text);
		return name.isEmpty() ? UNNAMED_COLUMN_KEY_PREFIX + columnNode.createID() : escapedName(name);
	}

	public static String leafNameOf(final NodeModel columnNode) {
		final String name = keyOfText(columnNode.getText());
		return name.isEmpty() ? UNNAMED_COLUMN_KEY_PREFIX + columnNode.createID() : name;
	}

	public static String escapedName(final String name) {
		return name.replace("\\", "\\\\").replace(PATH_SEPARATOR_CHARACTER, "\\" + PATH_SEPARATOR_CHARACTER);
	}

	public static String keyOfText(final Object text) {
		return text == null ? "" : HtmlUtils.htmlToPlain(text.toString()).trim();
	}

	public static boolean isKeyUsedByAnotherColumn(final NodeModel columnNode, final String key) {
		for (final NodeModel other : columnNodesOf(headerRootAbove(columnNode))) {
			if (other != columnNode && keyOf(other).equals(key))
				return true;
		}
		return false;
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

	public static boolean acceptsRenaming(final NodeModel columnNode, final String newName) {
		final NodeModel treeTableRoot = TreeTableNodes.treeTableRootOf(columnNode);
		if (!isColumnNode(columnNode) || treeTableRoot == null || newName == null || keyOfText(newName).isEmpty())
			return false;
		return columnsOf(treeTableRoot).acceptsRenaming(keyOf(columnNode), keyOf(columnNode, newName.trim()));
	}

	public static String keyInheritedFrom(final NodeModel formerColumnNode) {
		if (!isBelowHeaderRoot(formerColumnNode) || !formerColumnNode.hasChildren())
			return null;
		final String formerKey = keyOf(formerColumnNode);
		final NodeModel heir = heirOf(formerColumnNode);
		final String heirKey = keyOf(heir);
		if (formerKey.isEmpty() || heirKey.isEmpty() || formerKey.equals(heirKey))
			return null;
		for (final NodeModel columnNode : columnNodesOf(headerRootAbove(formerColumnNode))) {
			final String key = keyOf(columnNode);
			if (key.equals(formerKey) || columnNode != heir && key.equals(heirKey))
				return null;
		}
		return formerKey;
	}

	public static String keyReturnedTo(final NodeModel formerParent, final NodeModel removedNode) {
		if (!isBelowHeaderRoot(formerParent) || formerParent.hasChildren())
			return null;
		final String parentKey = keyOf(formerParent);
		final String removedKey = firstColumnKeyBelow(parentKey, removedNode);
		if (removedKey.equals(parentKey))
			return null;
		for (final NodeModel columnNode : columnNodesOf(headerRootAbove(formerParent))) {
			if (keyOf(columnNode).equals(removedKey))
				return null;
		}
		return removedKey;
	}

	private static String firstColumnKeyBelow(final String parentKey, final NodeModel node) {
		String key = pathKey(parentKey, segmentOf(node, node.getText()));
		for (NodeModel descendant = node; descendant.hasChildren();) {
			descendant = descendant.getChildAt(0);
			key = pathKey(key, segmentOf(descendant, descendant.getText()));
		}
		return key;
	}

	public static NodeModel heirOf(final NodeModel formerColumnNode) {
		return columnNodesOf(formerColumnNode).get(0);
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
