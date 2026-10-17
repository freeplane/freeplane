package org.freeplane.view.swing.map;

import java.awt.Rectangle;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.treetable.TreeTableColumns;
import org.freeplane.features.treetable.mindmapmode.TreeTableEditing;

class TreeTableHeaderMenu {
	private final MapView mapView;
	private final TreeTableCellEditor cellEditor;

	TreeTableHeaderMenu(final MapView mapView, final TreeTableCellEditor cellEditor) {
		this.mapView = mapView;
		this.cellEditor = cellEditor;
	}

	void show(final TreeTableView treeTable, final int columnIndex) {
		final NodeModel treeTableRoot = treeTable.getTreeTableRoot();
		final TreeTableColumns columns = treeTable.getColumns();
		final String attributeKey = columns.get(columnIndex).getAttributeKey();

		final JPopupMenu menu = new JPopupMenu();
		menu.add(item("treetable.rename_column", true,
			() -> SwingUtilities.invokeLater(() -> cellEditor.startEditingHeader(treeTable, columnIndex))));
		menu.add(item("treetable.add_column", true,
			() -> TreeTableEditing.addColumnRightOf(treeTableRoot, attributeKey)));
		menu.add(item("treetable.delete_column", columns.canRemove(),
			() -> TreeTableEditing.confirmAndDeleteColumn(treeTableRoot, attributeKey)));
		menu.add(item("treetable.move_column_left", columns.canMoveLeft(columnIndex),
			() -> TreeTableEditing.moveColumnLeft(treeTableRoot, attributeKey)));
		menu.add(item("treetable.move_column_right", columns.canMoveRight(columnIndex),
			() -> TreeTableEditing.moveColumnRight(treeTableRoot, attributeKey)));
		menu.addSeparator();
		menu.add(item("DeleteTreeTableAction.text", true,
			() -> TreeTableEditing.confirmAndDeleteTable(treeTableRoot)));

		final Rectangle button = treeTable.getHeaderMenuButtonBounds(columnIndex);
		menu.show(mapView, button.x, button.y + button.height);
	}

	private JMenuItem item(final String textKey, final boolean enabled, final Runnable action) {
		final JMenuItem item = new JMenuItem(TextUtils.getText(textKey));
		item.setEnabled(enabled);
		item.addActionListener(event -> action.run());
		return item;
	}
}
