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
package org.freeplane.view.swing.map.attribute;

import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;

import org.freeplane.features.attribute.Attribute;

/**
 * @author Dimitry Polivaev
 */
class ExtendedAttributeTableModelDecorator extends AttributeTableModel {
	private static final int AFTER_LAST_ROW = Integer.MAX_VALUE;
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	int newRow;
	final private AttributeView attributeView;

	public ExtendedAttributeTableModelDecorator(final AttributeView attrView) {
		super(attrView);
		this.attributeView = attrView;
		newRow = ExtendedAttributeTableModelDecorator.AFTER_LAST_ROW;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.view.mindmapview.attributeview.AttributeTableModelDecoratorAdapter
	 * #areAttributesVisible()
	 */
	@Override
	public boolean areAttributesVisible() {
		return getRowCount() != 0 || ! attributeView.isReduced();
	}

	@Override
	public void editingCanceled() {
		if (newRow != ExtendedAttributeTableModelDecorator.AFTER_LAST_ROW) {
			final int row = newRow;
			newRow = ExtendedAttributeTableModelDecorator.AFTER_LAST_ROW;
			fireTableRowsDeleted(row, row);
		}
	}

	@Override
	public int getRowCount() {
		if (newRow == ExtendedAttributeTableModelDecorator.AFTER_LAST_ROW) {
			return getNodeAttributeModel().getRowCount();
		}
		return getNodeAttributeModel().getRowCount() + 1;
	}

	@Override
	public Object getValueAt(final int row, final int col) {
		if (row < newRow) {
			return getNodeAttributeModel().getValueAt(row, col);
		}
		if (row == newRow) {
			return "";
		}
		return getNodeAttributeModel().getValueAt(row - 1, col);
	}

	public void insertRow(final int index) {
		newRow = index;
		fireTableRowsInserted(index, index);
	}

	@Override
	public boolean isCellEditable(final int row, final int col) {
		if (row != newRow) {
			return getAttributeController().canEdit();
		}
		return col == 0;
	}

	/**
	 */
	public void moveRowDown(final int row) {
		final Attribute attribute = getAttributeController().performRemoveRow(getNode(), getNodeAttributeModel(), row);
		getAttributeController().performInsertRow(getNode(), getNodeAttributeModel(), (row + 1), attribute.getName(),
		    attribute.getValue());
	}

	/**
	 */
	public void moveRowUp(final int row) {
		final Attribute attribute = getAttributeController().performRemoveRow(getNode(), getNodeAttributeModel(), row);
		getAttributeController().performInsertRow(getNode(), getNodeAttributeModel(), (row - 1), attribute.getName(),
		    attribute.getValue());
	}

	public Object removeRow(final int index) {
		return getAttributeController().performRemoveRow(getNode(), getNodeAttributeModel(), index);
	}

	@Override
	public void setValueAt(final Object o, final int row, final int col) {
		if (row != newRow) {
			if (col == 1 || o.toString().length() > 0) {
				final int rowInModel = row < newRow ? row : row - 1;
				getAttributeController().performSetValueAt(getNode(), getNodeAttributeModel(), o, rowInModel, col);
			}
			return;
		}
		else {
			newRow = ExtendedAttributeTableModelDecorator.AFTER_LAST_ROW;
			fireTableRowsDeleted(row, row);
			if (col == 0 && o != null && o.toString().length() > 0) {
				getAttributeController().performInsertRow(getNode(), getNodeAttributeModel(), row, o.toString(), "");
			}
			return;
		}
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		fireTableDataChanged();
	}

	@Override
	public void tableChanged(final TableModelEvent e) {
		super.tableChanged(e);
		fireTableChanged(new TableModelEvent(this, e.getFirstRow(), e.getLastRow(), e.getColumn(), e.getType()));
	}

	@Override
	public Attribute getAttribute(int row) {
		if (row < newRow) {
			return getNodeAttributeModel().getAttribute(row);
		}
		if (row == newRow) {
			return null;
		}
		return getNodeAttributeModel().getAttribute(row);

	}
}
