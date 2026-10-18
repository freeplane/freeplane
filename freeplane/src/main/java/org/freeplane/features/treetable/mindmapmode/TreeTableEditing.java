package org.freeplane.features.treetable.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.treetable.TreeTableColumn;
import org.freeplane.features.treetable.TreeTableColumnNames;
import org.freeplane.features.treetable.TreeTableColumns;
import org.freeplane.features.treetable.TreeTableModel;
import org.freeplane.features.treetable.TreeTableNodes;
import org.freeplane.main.mindmapmode.stylemode.EditStylesAction;

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

	public static void confirmAndDeleteColumn(final NodeModel treeTableRoot, final String attributeKey) {
		final TreeTableColumns columns = TreeTableModel.columnsOf(treeTableRoot);
		if (!columns.containsAttributeKey(attributeKey) || !columns.canRemove())
			return;
		final String message = TextUtils.format("treetable.confirm_delete_column", attributeKey);
		if (TreeTableDeletionConfirmation.isConfirmed(message))
			deleteColumn(treeTableRoot, attributeKey);
	}

	public static void confirmAndDeleteTable(final NodeModel treeTableRoot) {
		if (!TreeTableModel.isTreeTableRoot(treeTableRoot))
			return;
		if (TreeTableDeletionConfirmation.isConfirmed(TextUtils.getText("treetable.confirm_delete_table")))
			deleteTable(treeTableRoot);
	}

	private static void deleteColumn(final NodeModel treeTableRoot, final String attributeKey) {
		final TreeTableColumns columns = TreeTableModel.columnsOf(treeTableRoot);
		setColumns(treeTableRoot, columns.removedAt(columns.indexOfAttributeKey(attributeKey)),
			"deleteTreeTableColumn");
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

	public static void previewColumnWidth(final NodeModel treeTableRoot, final TreeTableColumns columnsBeforeResize,
			final String attributeKey, final Quantity<LengthUnit> width) {
		TreeTableModel.createFor(treeTableRoot).setColumns(columnsBeforeResize.resized(attributeKey, width));
	}

	public static void resizeColumn(final NodeModel treeTableRoot, final TreeTableColumns columnsBeforeResize,
			final String attributeKey, final Quantity<LengthUnit> width) {
		TreeTableModel.createFor(treeTableRoot).setColumns(columnsBeforeResize);
		final TreeTableColumns resized = columnsBeforeResize.resized(attributeKey, width);
		if (!resized.equals(columnsBeforeResize))
			setColumns(treeTableRoot, resized, "resizeTreeTableColumn");
	}

	public static void convertToTable(final NodeModel node) {
		if (TreeTableModel.isTreeTableRoot(node))
			return;
		TreeTableAppearance.ensureStylesExistIn(node.getMap());
		toggleTable(node);
	}

	public static void editTableStyle(final ActionEvent event, final MapModel map, final IStyle style,
			final EditStylesAction editStylesAction) {
		TreeTableAppearance.ensureStylesExistIn(map);
		editStylesAction.editStyle(event, style);
	}

	private static void deleteTable(final NodeModel treeTableRoot) {
		toggleTable(treeTableRoot);
	}

	private static void toggleTable(final NodeModel node) {
		final MapModel map = node.getMap();
		Controller.getCurrentModeController().execute(new ToggleTreeTableActor(node), map);
		LogicalStyleController.getController().refreshMapLaterUndoable(map);
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
