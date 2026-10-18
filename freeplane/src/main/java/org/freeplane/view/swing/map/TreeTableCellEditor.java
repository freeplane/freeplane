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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.mindmapmode.EditNodeBase;
import org.freeplane.features.text.mindmapmode.EditNodeBase.EditedComponent;
import org.freeplane.features.text.mindmapmode.EditNodeBase.IEditControl;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.treetable.TreeTableCellLayout;
import org.freeplane.features.treetable.mindmapmode.TreeTableEditing;

class TreeTableCellEditor extends MouseAdapter {
	private static final String CANCEL_EDITING = "treeTableCancelEditing";

	private final MapView mapView;

	private JTextField editor;
	private TreeTableCell editedCell;
	private boolean editsInDialog;

	TreeTableCellEditor(final MapView mapView) {
		this.mapView = mapView;
	}

	@Override
	public void mousePressed(final MouseEvent event) {
		if (event.isConsumed() || event.getButton() != MouseEvent.BUTTON1)
			return;
		final List<TreeTableView> treeTables = TreeTableView.allIn(mapView);
		final TreeTableCell cell = cellAt(treeTables, event.getX(), event.getY());
		if (cell == null) {
			stopEditing(true);
			return;
		}
		event.consume();
		if (event.getClickCount() > 1 || HtmlUtils.isHtml(currentValue(cell)))
			editInDialog(cell);
		else
			startEditing(cell);
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
		return editedCell != null && editedCell.getRow() == node
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
		editor.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				if (e.getClickCount() > 1)
					editInDialog(cell);
			}
		});
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

	private void editInDialog(final TreeTableCell cell) {
		if (editsInDialog)
			return;
		stopEditing(true);
		final EditNodeBase dialog = MTextController.getController().createEditor(cell.getRow(), cell.getRow(),
			currentValue(cell), new CellEditControl(cell), false, true, true);
		if (dialog == null)
			return;
		editsInDialog = true;
		dialog.show(SwingUtilities.getWindowAncestor(mapView));
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
		final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(cell.getRow());
		final int index = attributes.getAttributeIndex(cell.getAttributeKey());
		if (index < 0)
			return "";
		final Object value = attributes.getValue(index);
		return value != null ? value.toString() : "";
	}

	private void commitValue(final TreeTableCell cell, final String value) {
		final String simplifiedValue = TreeTableCellLayout.simplifiedValue(value);
		if (!currentValue(cell).equals(simplifiedValue))
			TreeTableEditing.setCellValue(cell.getRow(), cell.getAttributeKey(), simplifiedValue);
	}

	private class CellEditControl implements IEditControl {
		private final TreeTableCell cell;

		CellEditControl(final TreeTableCell cell) {
			this.cell = cell;
		}

		@Override
		public void cancel() {
			editsInDialog = false;
		}

		@Override
		public void ok(final String newText) {
			editsInDialog = false;
			commitValue(cell, newText);
		}

		@Override
		public void split(final String newText, final int position) {
		}

		@Override
		public boolean canSplit() {
			return false;
		}

		@Override
		public EditedComponent getEditType() {
			return EditedComponent.TEXT;
		}
	}
}
