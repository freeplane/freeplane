package org.freeplane.features.treetable.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeModel.Side;
import org.freeplane.features.map.mindmapmode.InsertionRelation;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.treetable.TreeTableCells;
import org.freeplane.features.treetable.TreeTableColumnNames;
import org.freeplane.features.treetable.TreeTableGridCompletion;
import org.freeplane.features.treetable.TreeTableHeader;
import org.freeplane.features.treetable.TreeTableModel;
import org.freeplane.features.treetable.TreeTableNodes;
import org.freeplane.features.treetable.TreeTableSheet;
import org.freeplane.features.treetable.TreeTableSheetImport;
import org.freeplane.main.mindmapmode.stylemode.EditStylesAction;

public class TreeTableEditing {
	private static boolean completesCells;

	public static void installListeners(final MapController mapController) {
		mapController.addNodeChangeListener(new TreeTableColumnRenaming());
		mapController.addMapChangeListener(new TreeTableColumnSplitting());
		mapController.addMapLifeCycleListener(new TreeTableCellMigrationOnLoad());
	}

	public static void selectCellContent(final NodeModel row, final String columnKey) {
		completeCells(row);
		final NodeModel cell = TreeTableCells.cellOf(row, columnKey);
		if (cell != null && cell.hasChildren())
			mapController().select(cell.getChildAt(0));
	}

	public static void renameColumn(final NodeModel columnNode, final String newKey) {
		if (TreeTableHeader.acceptsRenaming(columnNode, newKey))
			MTextController.getController().setNodeText(columnNode, newKey.trim());
	}

	static void renameColumnValues(final NodeModel columnNode, final String oldKey, final String newKey) {
		final NodeModel treeTableRoot = TreeTableNodes.treeTableRootOf(columnNode);
		if (treeTableRoot == null || oldKey.isEmpty() || newKey.isEmpty() || oldKey.equals(newKey)
				|| isUsedByAnotherColumn(columnNode, newKey))
			return;
		for (final NodeModel row : TreeTableNodes.rowsOf(treeTableRoot))
			renameCell(row, oldKey, newKey);
	}

	static void inheritColumnValues(final NodeModel formerColumnNode) {
		final String inheritedKey = TreeTableHeader.keyInheritedFrom(formerColumnNode);
		if (inheritedKey == null)
			return;
		final String heirKey = TreeTableHeader.keyOf(TreeTableHeader.heirOf(formerColumnNode));
		for (final NodeModel row : TreeTableNodes.rowsOf(TreeTableNodes.treeTableRootOf(formerColumnNode)))
			renameCell(row, inheritedKey, heirKey);
	}

	static void completeCells(final NodeModel changedNode) {
		final NodeModel treeTableRoot = TreeTableNodes.enclosingTreeTableRoot(changedNode);
		if (treeTableRoot == null || completesCells)
			return;
		completesCells = true;
		try {
			for (final NodeModel emptyCell : TreeTableGridCompletion.emptyCellsOutsideColumnsOf(treeTableRoot))
				mapController().deleteNode(emptyCell);
			for (final TreeTableGridCompletion.Gap gap : TreeTableGridCompletion.gapsOf(treeTableRoot))
				fill(gap);
		}
		finally {
			completesCells = false;
		}
	}

	private static void fill(final TreeTableGridCompletion.Gap gap) {
		if (gap.getEmptyCell() != null) {
			mapController().addNewNode(gap.getEmptyCell(), 0, node -> node.setUserObject(""));
			return;
		}
		final NodeModel row = gap.getRow();
		mapController().addNewNode(row, TreeTableCells.leadingCellCount(row), node -> {
			node.setUserObject(gap.getColumnKey());
			TreeTableCells.markAsCell(node);
			node.insert(new NodeModel("", node.getMap()));
		});
	}

	static void returnColumnValues(final NodeModel formerParent, final NodeModel removedNode) {
		final String returnedKey = TreeTableHeader.keyReturnedTo(formerParent, removedNode);
		if (returnedKey == null)
			return;
		final String parentKey = TreeTableHeader.keyOf(formerParent);
		for (final NodeModel row : TreeTableNodes.rowsOf(TreeTableNodes.treeTableRootOf(formerParent)))
			renameCell(row, returnedKey, parentKey);
	}

	private static boolean isUsedByAnotherColumn(final NodeModel columnNode, final String key) {
		for (final NodeModel other : TreeTableHeader.columnNodesOf(TreeTableHeader.headerRootAbove(columnNode))) {
			if (other != columnNode && TreeTableHeader.keyOf(other).equals(key))
				return true;
		}
		return false;
	}

	public static void addColumnRightOf(final NodeModel columnNode) {
		if (!TreeTableHeader.isBelowHeaderRoot(columnNode))
			return;
		final NodeModel parent = columnNode.getParentNode();
		insertColumn(parent, parent.getIndex(columnNode) + 1);
	}

	private static void insertColumn(final NodeModel parent, final int index) {
		final NodeModel treeTableRoot = TreeTableNodes.treeTableRootOf(parent);
		final String name = TreeTableColumnNames.unusedName(TextUtils.getText("treetable.new_column"),
			TreeTableModel.columnsOf(treeTableRoot));
		mapController().addNewNode(parent, index, node -> node.setUserObject(name));
	}

	public static void confirmAndDeleteColumn(final NodeModel columnNode) {
		if (!TreeTableHeader.canRemoveColumn(columnNode))
			return;
		final String key = TreeTableHeader.keyOf(columnNode);
		final String message = TextUtils.format("treetable.confirm_delete_column", key);
		if (TreeTableDeletionConfirmation.isConfirmed(message))
			deleteColumn(columnNode, key);
	}

	private static void deleteColumn(final NodeModel columnNode, final String key) {
		final NodeModel treeTableRoot = TreeTableNodes.treeTableRootOf(columnNode);
		mapController().deleteNode(columnNode);
		if (TreeTableModel.columnsOf(treeTableRoot).containsAttributeKey(key))
			return;
		for (final NodeModel row : TreeTableNodes.rowsOf(treeTableRoot))
			removeCell(row, key);
	}

	public static void moveColumnLeft(final NodeModel columnNode) {
		if (TreeTableHeader.canMoveColumnLeft(columnNode))
			mapController().moveNode(columnNode, columnNode.getParentNode().getIndex(columnNode) - 1);
	}

	public static void moveColumnRight(final NodeModel columnNode) {
		if (TreeTableHeader.canMoveColumnRight(columnNode))
			mapController().moveNode(columnNode, columnNode.getParentNode().getIndex(columnNode) + 1);
	}

	public static void resizeColumn(final NodeModel columnNode, final Quantity<LengthUnit> width) {
		final MNodeStyleController styleController = (MNodeStyleController) NodeStyleController.getController();
		styleController.setMinNodeWidth(columnNode, width);
		styleController.setMaxNodeWidth(columnNode, width);
		LogicalStyleController.getController().refreshMapLaterUndoable(columnNode.getMap());
	}

	public static void toggleTextWrap(final NodeModel treeTableRoot) {
		final boolean wrapped = TreeTableModel.wrapsText(treeTableRoot);
		Controller.getCurrentModeController().execute(new IActor() {
			@Override
			public void act() {
				TreeTableModel.createFor(treeTableRoot).setTextWrapped(!wrapped);
			}

			@Override
			public void undo() {
				TreeTableModel.createFor(treeTableRoot).setTextWrapped(wrapped);
			}

			@Override
			public String getDescription() {
				return "toggleTreeTableTextWrap";
			}
		}, treeTableRoot.getMap());
		LogicalStyleController.getController().refreshMapLaterUndoable(treeTableRoot.getMap());
	}

	public static void convertToTable(final NodeModel node) {
		if (!TreeTableNodes.canConvertToTable(node))
			return;
		TreeTableAppearance.ensureStylesExistIn(node.getMap());
		toggleTable(node);
		final NodeModel header = mapController().addNewNode(node, 0, headerNode -> {
			headerNode.setUserObject(TextUtils.getText("treetable.header"));
			LogicalStyleModel.createExtension(headerNode).setStyle(TreeTableHeader.style());
		});
		if (header != null)
			insertColumn(header, 0);
	}

	public static void pasteAsTable(final NodeModel target, final Side side, final TreeTableSheet sheet) {
		final NodeModel parent = side.isSibling() ? target.getParentNode() : target;
		if (parent == null || !TreeTableNodes.canConvertToTable(parent))
			return;
		final TreeTableSheetImport table = TreeTableSheetImport.of(sheet, TextUtils.getText("treetable.new_column"));
		if (table == null)
			return;
		final MapModel map = target.getMap();
		TreeTableAppearance.ensureStylesExistIn(map);
		final NodeModel treeTableRoot = mapController().newNode(table.getRootContent(), map);
		TreeTableModel.createFor(treeTableRoot);
		TreeTableAppearance.applyTo(treeTableRoot);
		final NodeModel header = mapController().newNode(TextUtils.getText("treetable.header"), map);
		LogicalStyleModel.createExtension(header).setStyle(TreeTableHeader.style());
		treeTableRoot.insert(header);
		insertItems(header, table.getColumns());
		insertItems(treeTableRoot, table.getRows());
		treeTableRoot.setSide(side.isSibling() ? target.getSide() : side);
		mapController().insertNode(treeTableRoot, target, InsertionRelation.bySide(side));
		LogicalStyleController.getController().refreshMapLaterUndoable(map);
	}

	private static void insertItems(final NodeModel parent, final List<TreeTableSheetImport.Item> items) {
		for (final TreeTableSheetImport.Item item : items) {
			final NodeModel node = mapController().newNode(item.getContent(), parent.getMap());
			for (final Map.Entry<String, String> value : item.getValueContents().entrySet())
				node.insert(TreeTableCells.newCell(parent.getMap(), value.getKey(), value.getValue()));
			parent.insert(node);
			insertItems(node, item.getChildren());
		}
	}

	public static void confirmAndDeleteTable(final NodeModel treeTableRoot) {
		if (!TreeTableModel.isTreeTableRoot(treeTableRoot))
			return;
		if (TreeTableDeletionConfirmation.isConfirmed(TextUtils.getText("treetable.confirm_delete_table")))
			deleteTable(treeTableRoot);
	}

	private static void deleteTable(final NodeModel treeTableRoot) {
		final NodeModel header = TreeTableHeader.headerOf(treeTableRoot);
		if (header != null)
			mapController().deleteNode(header);
		toggleTable(treeTableRoot);
	}

	public static void editTableStyle(final ActionEvent event, final MapModel map, final IStyle style,
			final EditStylesAction editStylesAction) {
		TreeTableAppearance.ensureStylesExistIn(map);
		editStylesAction.editStyle(event, style);
	}

	private static void toggleTable(final NodeModel node) {
		final MapModel map = node.getMap();
		Controller.getCurrentModeController().execute(new ToggleTreeTableActor(node), map);
		LogicalStyleController.getController().refreshMapLaterUndoable(map);
	}

	private static void renameCell(final NodeModel row, final String oldKey, final String newKey) {
		final NodeModel cell = TreeTableCells.cellOf(row, oldKey);
		if (cell != null && TreeTableCells.cellOf(row, newKey) == null)
			MTextController.getController().setNodeText(cell, newKey);
	}

	private static void removeCell(final NodeModel row, final String key) {
		final NodeModel cell = TreeTableCells.cellOf(row, key);
		if (cell != null)
			mapController().deleteNode(cell);
	}

	private static MMapController mapController() {
		return (MMapController) Controller.getCurrentModeController().getMapController();
	}

	private TreeTableEditing() {
	}
}
