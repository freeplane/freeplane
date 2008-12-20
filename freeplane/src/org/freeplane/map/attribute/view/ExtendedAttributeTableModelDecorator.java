/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.map.attribute.view;

import javax.swing.event.ChangeEvent;

import org.freeplane.map.attribute.Attribute;

/**
 * @author Dimitry Polivaev
 */
class ExtendedAttributeTableModelDecorator extends AttributeTableModelDecoratorAdapter {
	private static final int AFTER_LAST_ROW = Integer.MAX_VALUE;
	int newRow;

	public ExtendedAttributeTableModelDecorator(final AttributeView attrView) {
		super(attrView);
		newRow = ExtendedAttributeTableModelDecorator.AFTER_LAST_ROW;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.view.mindmapview.attributeview.AttributeTableModelDecoratorAdapter
	 * #areAttributesVisible()
	 */
	@Override
	public boolean areAttributesVisible() {
		return getRowCount() != 0;
	}

	@Override
	public void editingCanceled() {
		if (newRow != ExtendedAttributeTableModelDecorator.AFTER_LAST_ROW) {
			final int row = newRow;
			newRow = ExtendedAttributeTableModelDecorator.AFTER_LAST_ROW;
			fireTableRowsDeleted(row, row);
		}
	}

	public int getRowCount() {
		if (newRow == ExtendedAttributeTableModelDecorator.AFTER_LAST_ROW) {
			return nodeAttributeModel.getRowCount();
		}
		return nodeAttributeModel.getRowCount() + 1;
	}

	public Object getValueAt(final int row, final int col) {
		if (row < newRow) {
			return nodeAttributeModel.getValueAt(row, col);
		}
		if (row == newRow) {
			return "";
		}
		return nodeAttributeModel.getValueAt(row - 1, col);
	}

	public void insertRow(final int index) {
		newRow = index;
		fireTableRowsInserted(index, index);
	}

	@Override
	public boolean isCellEditable(final int row, final int col) {
		if (row != newRow) {
			final int rowInModel = row < newRow ? row : row - 1;
			return nodeAttributeModel.isCellEditable(rowInModel, col);
		}
		return col == 0;
	}

	/**
	 */
	public void moveRowDown(final int row) {
		final Attribute attribute = (Attribute) nodeAttributeModel.removeRow(row);
		nodeAttributeModel.insertRow(row + 1, attribute.getName(), attribute.getValue());
	}

	/**
	 */
	public void moveRowUp(final int row) {
		final Attribute attribute = (Attribute) nodeAttributeModel.removeRow(row);
		nodeAttributeModel.insertRow(row - 1, attribute.getName(), attribute.getValue());
	}

	public Object removeRow(final int index) {
		return nodeAttributeModel.removeRow(index);
	}

	@Override
	public void setValueAt(final Object o, final int row, final int col) {
		if (row != newRow) {
			if (col == 1 || o.toString().length() > 0) {
				final int rowInModel = row < newRow ? row : row - 1;
				nodeAttributeModel.setValueAt(o, rowInModel, col);
			}
			return;
		}
		else {
			newRow = ExtendedAttributeTableModelDecorator.AFTER_LAST_ROW;
			fireTableRowsDeleted(row, row);
			if (col == 0 && o != null && o.toString().length() > 0) {
				nodeAttributeModel.insertRow(row, o.toString(), "");
			}
			return;
		}
	}

	public void stateChanged(final ChangeEvent e) {
		fireTableDataChanged();
	}
}
