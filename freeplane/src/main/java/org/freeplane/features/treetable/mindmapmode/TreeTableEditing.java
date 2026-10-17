package org.freeplane.features.treetable.mindmapmode;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.treetable.TreeTableColumn;
import org.freeplane.features.treetable.TreeTableColumnNames;
import org.freeplane.features.treetable.TreeTableColumns;
import org.freeplane.features.treetable.TreeTableModel;
import org.freeplane.features.treetable.TreeTableNodes;

public class TreeTableEditing {
	private static final int ATTRIBUTE_NAME_COLUMN = 0;
	private static final int ATTRIBUTE_VALUE_COLUMN = 1;

	public static TreeTableColumns withNewColumn(final TreeTableColumns columns) {
		return columns.appended(newColumn(columns));
	}

	private static TreeTableColumn newColumn(final TreeTableColumns columns) {
		final String prefix = TextUtils.getText("treetable.new_column");
		return new TreeTableColumn(TreeTableColumnNames.unusedName(prefix, columns));
	}

	public static void setCellValue(final NodeModel row, final String attributeKey, final String value) {
		final MAttributeController attributeController = attributeController();
		final NodeAttributeTableModel attributes = attributeController.createAttributeTableModel(row);
		final int index = attributes.getAttributeIndex(attributeKey);
		if (index < 0)
			attributeController.performInsertRow(row, attributes, attributes.getRowCount(), attributeKey, value);
		else
			attributeController.performSetValueAt(row, attributes, value, index, ATTRIBUTE_VALUE_COLUMN);
	}

	public static void renameColumn(final NodeModel treeTableRoot, final String oldAttributeKey,
			final String newAttributeKey) {
		final String trimmedKey = newAttributeKey.trim();
		final TreeTableColumns columns = TreeTableModel.columnsOf(treeTableRoot);
		if (!columns.acceptsRenaming(oldAttributeKey, trimmedKey))
			return;
		setColumns(treeTableRoot, columns.renamed(oldAttributeKey, trimmedKey), "renameTreeTableColumn");
		for (final NodeModel row : TreeTableNodes.rowsOf(treeTableRoot))
			renameAttribute(row, oldAttributeKey, trimmedKey);
	}

	public static void addColumnRightOf(final NodeModel treeTableRoot, final String attributeKey) {
		final TreeTableColumns columns = TreeTableModel.columnsOf(treeTableRoot);
		final int index = columns.indexOfAttributeKey(attributeKey);
		if (index < 0)
			return;
		setColumns(treeTableRoot, columns.insertedRightOf(index, newColumn(columns)), "addTreeTableColumn");
	}

	public static void deleteColumn(final NodeModel treeTableRoot, final String attributeKey) {
		final TreeTableColumns columns = TreeTableModel.columnsOf(treeTableRoot);
		final int index = columns.indexOfAttributeKey(attributeKey);
		if (index < 0 || !columns.canRemove())
			return;
		setColumns(treeTableRoot, columns.removedAt(index), "deleteTreeTableColumn");
		for (final NodeModel row : TreeTableNodes.rowsOf(treeTableRoot))
			removeAttribute(row, attributeKey);
	}

	public static void moveColumnLeft(final NodeModel treeTableRoot, final String attributeKey) {
		final TreeTableColumns columns = TreeTableModel.columnsOf(treeTableRoot);
		final int index = columns.indexOfAttributeKey(attributeKey);
		if (!columns.canMoveLeft(index))
			return;
		setColumns(treeTableRoot, columns.movedFromTo(index, index - 1), "moveTreeTableColumnLeft");
	}

	public static void moveColumnRight(final NodeModel treeTableRoot, final String attributeKey) {
		final TreeTableColumns columns = TreeTableModel.columnsOf(treeTableRoot);
		final int index = columns.indexOfAttributeKey(attributeKey);
		if (!columns.canMoveRight(index))
			return;
		setColumns(treeTableRoot, columns.movedFromTo(index, index + 1), "moveTreeTableColumnRight");
	}

	public static void deleteTable(final NodeModel treeTableRoot) {
		if (!TreeTableModel.isTreeTableRoot(treeTableRoot))
			return;
		Controller.getCurrentModeController().execute(new ToggleTreeTableActor(treeTableRoot),
			treeTableRoot.getMap());
	}

	private static void setColumns(final NodeModel treeTableRoot, final TreeTableColumns columns,
			final String description) {
		Controller.getCurrentModeController().execute(
			new SetTreeTableColumnsActor(treeTableRoot, columns, description), treeTableRoot.getMap());
	}

	private static void renameAttribute(final NodeModel row, final String oldAttributeKey,
			final String newAttributeKey) {
		final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(row);
		final int index = attributes.getAttributeIndex(oldAttributeKey);
		if (index >= 0)
			attributeController().performSetValueAt(row, attributes, newAttributeKey, index, ATTRIBUTE_NAME_COLUMN);
	}

	private static void removeAttribute(final NodeModel row, final String attributeKey) {
		final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(row);
		final int index = attributes.getAttributeIndex(attributeKey);
		if (index >= 0)
			attributeController().performRemoveRow(row, attributes, index);
	}

	private static MAttributeController attributeController() {
		return (MAttributeController) AttributeController.getController(Controller.getCurrentModeController());
	}

	private TreeTableEditing() {
	}
}
