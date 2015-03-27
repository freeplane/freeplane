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
package org.freeplane.features.attribute;

import javax.swing.table.AbstractTableModel;

import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.collection.IListModel;

/**
 * @author Dimitry Polivaev
 */
public class AttributeRegistryTableModel extends AbstractTableModel {
	static private String attributeColumnName = null;
	static private String editorColumnName = null;
	static private String restrictionColumnName = null;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static private String visibilityColumnName = null;
	final private String allAttributes = TextUtils.getText("attributes_all");
	final private AttributeRegistry attributeRegistry;

	AttributeRegistryTableModel(final AttributeRegistry registry) {
		attributeRegistry = registry;
	}

	/**
	 */
	public void fireRestrictionsUpdated(final int row) {
		fireTableRowsUpdated(row + 1, row + 1);
	}

	@Override
	public void fireTableCellUpdated(final int row, final int column) {
		super.fireTableCellUpdated(row + 1, column);
	}

	public void fireTableRowsDeleted() {
		if (getRowCount() > 1) {
			fireTableRowsDeleted(1, getRowCount() - 1);
		}
	}

	@Override
	public void fireTableRowsDeleted(final int firstRow, final int lastRow) {
		super.fireTableRowsDeleted(firstRow + 1, lastRow + 1);
	}

	@Override
	public void fireTableRowsInserted(final int firstRow, final int lastRow) {
		super.fireTableRowsInserted(firstRow + 1, lastRow + 1);
	}

	@Override
	public void fireTableRowsUpdated(final int firstRow, final int lastRow) {
		super.fireTableRowsUpdated(firstRow + 1, lastRow + 1);
	}

	/**
	 */
	public void fireVisibilityUpdated(final int row) {
		fireTableCellUpdated(row + 1, 1);
	}

	@Override
	public Class<?> getColumnClass(final int c) {
		switch (c) {
			case 0:
				return String.class;
			case 1:
				return Boolean.class;
			case 2:
				return Boolean.class;
			case 3:
				return IListModel.class;
		}
		return Object.class;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return 4;
	}

	@Override
	public String getColumnName(final int column) {
		switch (column) {
			case 0:
				if (AttributeRegistryTableModel.attributeColumnName == null) {
					AttributeRegistryTableModel.attributeColumnName = TextUtils.getText("attributes_attribute");
				}
				return AttributeRegistryTableModel.attributeColumnName;
			case 1:
				if (AttributeRegistryTableModel.visibilityColumnName == null) {
					AttributeRegistryTableModel.visibilityColumnName = TextUtils.getText("attributes_visible");
				}
				return AttributeRegistryTableModel.visibilityColumnName;
			case 2:
				if (AttributeRegistryTableModel.restrictionColumnName == null) {
					AttributeRegistryTableModel.restrictionColumnName = TextUtils.getText("attributes_restriction");
				}
				return AttributeRegistryTableModel.restrictionColumnName;
			case 3:
				if (AttributeRegistryTableModel.editorColumnName == null) {
					AttributeRegistryTableModel.editorColumnName = TextUtils.getText("attributes_edit");
				}
				return AttributeRegistryTableModel.editorColumnName;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return attributeRegistry.size() + 1;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, final int col) {
		if (row == 0 && col < 2) {
			if (col == 0) {
				return allAttributes;
			}
			return null;
		}
		row--;
		switch (col) {
			case 0:
				return attributeRegistry.getKey(row);
			case 1:
				return attributeRegistry.getElement(row).getVisibilityModel();
			case 2:
				return attributeRegistry.getRestriction(row);
			case 3:
				return attributeRegistry.getValues(row);
		}
		return null;
	}

	@Override
	public boolean isCellEditable(final int row, final int col) {
		return col >= 1;
	}

	@Override
	public void setValueAt(final Object o, final int row, final int col) {
		if (row == 0 && col != 2) {
			return;
		}
		if (col == 3) {
			return;
		}
		final Boolean value = (Boolean) o;
		switch (col) {
			case 1:
				attributeRegistry.setVisibilityModel(row - 1, value);
				break;
			case 2:
				attributeRegistry.setRestrictionModel(row - 1, value);
				break;
		}
	}
}
