package org.freeplane.features.treetable.mindmapmode;

import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.treetable.TreeTableColumns;
import org.freeplane.features.treetable.TreeTableModel;

public class TreeTableEditing {
	private static final int ATTRIBUTE_NAME_COLUMN = 0;
	private static final int ATTRIBUTE_VALUE_COLUMN = 1;

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
		Controller.getCurrentModeController().execute(new SetTreeTableColumnsActor(treeTableRoot,
			columns.renamed(oldAttributeKey, trimmedKey), "renameTreeTableColumn"), treeTableRoot.getMap());
		renameRowAttributes(treeTableRoot, oldAttributeKey, trimmedKey);
	}

	private static void renameRowAttributes(final NodeModel parent, final String oldAttributeKey,
			final String newAttributeKey) {
		for (final NodeModel row : parent.getChildren()) {
			renameAttribute(row, oldAttributeKey, newAttributeKey);
			if (!TreeTableModel.isTreeTableRoot(row))
				renameRowAttributes(row, oldAttributeKey, newAttributeKey);
		}
	}

	private static void renameAttribute(final NodeModel row, final String oldAttributeKey,
			final String newAttributeKey) {
		final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(row);
		final int index = attributes.getAttributeIndex(oldAttributeKey);
		if (index >= 0)
			attributeController().performSetValueAt(row, attributes, newAttributeKey, index, ATTRIBUTE_NAME_COLUMN);
	}

	private static MAttributeController attributeController() {
		return (MAttributeController) AttributeController.getController(Controller.getCurrentModeController());
	}

	private TreeTableEditing() {
	}
}
