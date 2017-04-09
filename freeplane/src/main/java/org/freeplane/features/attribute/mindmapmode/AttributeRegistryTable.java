/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.attribute.mindmapmode;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.collection.IListModel;

/**
 * @author Dimitry Polivaev
 */
class AttributeRegistryTable extends JTable {
	static private class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		final private JButton editButton;
		private Object value;

		public ButtonEditor(final Action action) {
			editButton = new JButton(action);
			editButton.setFocusable(false);
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		public Object getCellEditorValue() {
			return value;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax
		 * .swing.JTable, java.lang.Object, boolean, int, int)
		 */
		public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected,
		                                             final int row, final int column) {
			this.value = value;
			return editButton;
		}
	}

	static private class ButtonRenderer implements TableCellRenderer {
		final private JButton renderingEditButton;

		public ButtonRenderer(final Icon image, final String toolTip) {
			renderingEditButton = new JButton(image);
			renderingEditButton.setFocusable(false);
			renderingEditButton.setToolTipText(toolTip);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.table.TableCellRenderer#getTableCellRendererComponent
		 * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(final JTable table, final Object value,
		                                               final boolean isSelected, final boolean hasFocus, final int row,
		                                               final int column) {
			return renderingEditButton;
		}
	}

	private class ToggleAllAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ToggleAllAction() {
			super(null, AttributeRegistryTable.checkBoxImage);
		}

		public void actionPerformed(final ActionEvent e) {
			final int rowCount = getRowCount();
			if (rowCount <= 1) {
				return;
			}
			Boolean checked = (Boolean) getValueAt(1, 1);
			checked = Boolean.valueOf(!checked.booleanValue());
			for (int i = 1; i < rowCount; i++) {
				setValueAt(checked, i, 1);
			}
		}
	}

	static final private Icon checkBoxImage = new ImageIcon(ResourceController.getResourceController().getIconResource(
	    "/images/checkbox12.png"));
	private static final ButtonRenderer editButtonRenderer = new ButtonRenderer(AttributeManagerDialog.editButtonImage,
	    TextUtils.getText("attributes_edit_tooltip"));
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private AttributeManagerDialog.EditListAction editListAction;
	final private ButtonEditor selectAllButtonEditor;
	final private ButtonRenderer selectAllButtonRenderer;

	public AttributeRegistryTable(final AttributeManagerDialog.EditListAction editListAction) {
		super();
		this.editListAction = editListAction;
		getTableHeader().setReorderingAllowed(false);
		selectAllButtonRenderer = new ButtonRenderer(AttributeRegistryTable.checkBoxImage, TextUtils
		    .getText("attributes_select_all_tooltip"));
		selectAllButtonEditor = new ButtonEditor(new ToggleAllAction());
		setDefaultEditor(IListModel.class, new ButtonEditor(editListAction));
		setDefaultRenderer(IListModel.class, AttributeRegistryTable.editButtonRenderer);
		setRowHeight(20);
		setRowSelectionAllowed(false);
	}

	@Override
	public TableCellEditor getCellEditor(final int row, final int column) {
		if (row == 0 && column == 1) {
			return selectAllButtonEditor;
		}
		return super.getCellEditor(row, column);
	}

	@Override
	public TableCellRenderer getCellRenderer(final int row, final int column) {
		if (row == 0 && column == 1) {
			return selectAllButtonRenderer;
		}
		final TableCellRenderer tableCellRenderer = super.getCellRenderer(row, column);
		if (tableCellRenderer instanceof JLabel) {
			final JLabel label = (JLabel) tableCellRenderer;
			if (row == 0) {
				label.setHorizontalAlignment(SwingConstants.CENTER);
			}
			else {
				label.setHorizontalAlignment(SwingConstants.LEFT);
			}
		}
		else if (tableCellRenderer instanceof JComponent) {
			final JComponent label = (JComponent) tableCellRenderer;
			switch (column) {
				case 1:
					label.setToolTipText(TextUtils.getText("attributes_visible_tooltip"));
					break;
				case 2:
					if (row == 0) {
						label.setToolTipText(TextUtils.getText("attributes_restricted_attributes_tooltip"));
					}
					else {
						label.setToolTipText(TextUtils.getText("attributes_restricted_values_tooltip"));
					}
					break;
			}
		}
		return tableCellRenderer;
	}

	@Override
	public Component prepareEditor(final TableCellEditor editor, final int row, final int column) {
		if (column == 3) {
			final IListModel list = (IListModel) getModel().getValueAt(row, column);
			final String title = getModel().getValueAt(row, 0).toString();
			final String labelText = TextUtils.getText("attribute_list_box_label_text");
			editListAction.setListBoxModel(title, labelText, list);
		}
		return super.prepareEditor(editor, row, column);
	}

	@Override
	public void setModel(final TableModel dataModel) {
		super.setModel(dataModel);
		if (dataModel.getColumnCount() >= 1) {
			for (int i = 1; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setMinWidth(20);
				final int prefWidth = getTableHeader().getDefaultRenderer().getTableCellRendererComponent(this,
				    getColumnName(i), false, false, -1, i).getPreferredSize().width;
				getColumnModel().getColumn(i).setPreferredWidth(prefWidth);
			}
		}
	}
}
