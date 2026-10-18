package org.freeplane.features.treetable.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.treetable.TreeTableColumnNames;
import org.freeplane.features.treetable.TreeTableHeader;
import org.freeplane.features.treetable.TreeTableModel;
import org.freeplane.features.treetable.TreeTableNodes;
import org.freeplane.main.mindmapmode.stylemode.EditStylesAction;

public class TreeTableEditing {
	private static final int ATTRIBUTE_NAME_COLUMN = 0;
	private static final int ATTRIBUTE_VALUE_COLUMN = 1;

	public static void installColumnRenaming(final MapController mapController) {
		mapController.addNodeChangeListener(new TreeTableColumnRenaming());
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
			renameAttribute(row, oldKey, newKey);
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
			removeAttribute(row, key);
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

	private static MMapController mapController() {
		return (MMapController) Controller.getCurrentModeController().getMapController();
	}

	private static MAttributeController attributeController() {
		return (MAttributeController) AttributeController.getController(Controller.getCurrentModeController());
	}

	private TreeTableEditing() {
	}
}
