package org.freeplane.view.swing.map;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.treetable.mindmapmode.TreeTableEditing;

class TreeTableCellEditor extends MouseAdapter {
	private static final String CANCEL_EDITING = "treeTableCancelEditing";

	private final MapView mapView;
	private final TreeTableHeaderMenu headerMenu;

	private JTextField editor;
	private TreeTableCell editedCell;

	TreeTableCellEditor(final MapView mapView) {
		this.mapView = mapView;
		this.headerMenu = new TreeTableHeaderMenu(mapView, this);
	}

	void startEditingHeader(final TreeTableView treeTable, final int columnIndex) {
		final String attributeKey = treeTable.getColumns().get(columnIndex).getAttributeKey();
		startEditing(TreeTableCell.header(treeTable.getTreeTableRoot(), attributeKey,
			treeTable.getHeaderTextBounds(columnIndex)));
	}

	@Override
	public void mousePressed(final MouseEvent event) {
		if (event.getButton() != MouseEvent.BUTTON1)
			return;
		final List<TreeTableView> treeTables = TreeTableView.allIn(mapView);
		if (showHeaderMenuAt(treeTables, event.getX(), event.getY())) {
			event.consume();
			return;
		}
		final TreeTableCell cell = cellAt(treeTables, event.getX(), event.getY());
		if (cell == null) {
			stopEditing(true);
			return;
		}
		event.consume();
		startEditing(cell);
	}

	private boolean showHeaderMenuAt(final List<TreeTableView> treeTables, final int x, final int y) {
		for (final TreeTableView treeTable : treeTables) {
			final int columnIndex = treeTable.headerMenuColumnAt(x, y);
			if (columnIndex >= 0) {
				stopEditing(true);
				headerMenu.show(treeTable, columnIndex);
				return true;
			}
		}
		return false;
	}

	private TreeTableCell cellAt(final List<TreeTableView> treeTables, final int x, final int y) {
		for (final TreeTableView treeTable : treeTables) {
			final TreeTableCell cell = treeTable.cellAt(x, y);
			if (cell != null)
				return cell;
		}
		return null;
	}

	boolean isEditing(final NodeModel node, final String attributeKey) {
		return editedCell != null && editedCell.getNode() == node
			&& editedCell.getAttributeKey().equals(attributeKey);
	}

	private void startEditing(final TreeTableCell cell) {
		stopEditing(true);
		editedCell = cell;
		editor = new JTextField(currentValue(cell));
		editor.setBounds(cell.getBounds());
		editor.setOpaque(true);
		editor.setBackground(UIManager.getColor("TextField.background"));
		editor.selectAll();
		editor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				stopEditing(true);
			}
		});
		editor.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent e) {
				stopEditing(true);
			}
		});
		editor.getInputMap(JComponent.WHEN_FOCUSED)
			.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CANCEL_EDITING);
		editor.getActionMap().put(CANCEL_EDITING, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				stopEditing(false);
			}
		});
		mapView.add(editor);
		editor.requestFocusInWindow();
		mapView.repaint(cell.getBounds());
	}

	private void stopEditing(final boolean commit) {
		if (editor == null)
			return;
		final JTextField stopped = editor;
		final TreeTableCell cell = editedCell;
		editor = null;
		editedCell = null;
		if (commit)
			commitValue(cell, stopped.getText());
		mapView.remove(stopped);
		mapView.repaint(cell.getBounds());
	}

	private String currentValue(final TreeTableCell cell) {
		if (cell.isHeader())
			return cell.getAttributeKey();
		final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(cell.getNode());
		final int index = attributes.getAttributeIndex(cell.getAttributeKey());
		if (index < 0)
			return "";
		final Object value = attributes.getValue(index);
		return value != null ? value.toString() : "";
	}

	private void commitValue(final TreeTableCell cell, final String value) {
		if (currentValue(cell).equals(value))
			return;
		if (cell.isHeader())
			TreeTableEditing.renameColumn(cell.getNode(), cell.getAttributeKey(), value);
		else
			TreeTableEditing.setCellValue(cell.getNode(), cell.getAttributeKey(), value);
	}
}
