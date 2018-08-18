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

import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;

import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;

/**
 * @author Dimitry Polivaev
 */
class ReducedAttributeTableModelDecorator extends AttributeTableModel {
	private static final long serialVersionUID = 1L;
	private Vector<Integer> index = null;
	private int visibleRowCount;

	ReducedAttributeTableModelDecorator(final AttributeView attrView) {
		super(attrView);
		rebuildTableModel();
	}

	/*
	 * (non-Javadoc)
	 * @seefreeplane.modes.attributes.AttributeTableModel#addRow(freeplane.modes.
	 * attributes.Attribute)
	 */
	public void addRow(final Attribute newAttribute) {
		throw new Error();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.view.mindmapview.attributeview.AttributeTableModelDecoratorAdapter
	 * #areAttributesVisible()
	 */
	@Override
	public boolean areAttributesVisible() {
		return getRowCount() != 0;
	}

	private int calcRow(final int row) {
		return index.get(row).intValue();
	}

	private Vector<Integer> getIndex() {
		if (index == null && getAttributeRegistry().getVisibleElementsNumber() > 0) {
			index = new Vector<Integer>(getNodeAttributeModel().getRowCount(), 10);
		}
		return index;
	}

	@Override
	public int getRowCount() {
		return visibleRowCount;
	}

	@Override
	public Object getValueAt(final int row, final int col) {
		if(index == null)
			return null;
		return getNodeAttributeModel().getValueAt(calcRow(row), col);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.modes.attributes.AttributeTableModel#insertRow(int,
	 * freeplane.modes.attributes.Attribute)
	 */
	public void insertRow(final int index, final Attribute newAttribute) {
		throw new Error();
	}

	@Override
	public boolean isCellEditable(final int row, final int col) {
		if (getAttributeController() instanceof MAttributeController) {
			return col == 1;
		}
		return false;
	}

	private void rebuildTableModel() {
		getIndex();
		if (index != null) {
			visibleRowCount = 0;
			index.clear();
			for (int i = 0; i < getNodeAttributeModel().getRowCount(); i++) {
				final String name = (String) getNodeAttributeModel().getValueAt(i, 0);
				if (getAttributeRegistry().getElement(name).isVisible()) {
					index.add(new Integer(i));
					visibleRowCount++;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.modes.attributes.AttributeTableModel#removeRow(int)
	 */
	public Object removeRow(final int index) {
		throw new Error();
	}

	@Override
	public void setValueAt(final Object o, final int row, final int col) {
		getAttributeController().performSetValueAt(getNode(), getNodeAttributeModel(), o, calcRow(row), col);
		fireTableCellUpdated(row, col);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	@Override
	public void stateChanged(final ChangeEvent e) {
		rebuildTableModel();
		if (index != null) {
			fireTableDataChanged();
		}
	}

	@Override
	public void tableChanged(final TableModelEvent e) {
		super.tableChanged(e);
		if (e.getType() != TableModelEvent.UPDATE || e.getColumn() != 0) {
			rebuildTableModel();
		}
		fireTableDataChanged();
	}
}
