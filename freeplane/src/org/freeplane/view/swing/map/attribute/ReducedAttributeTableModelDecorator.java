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

import org.freeplane.map.attribute.Attribute;

/**
 * @author Dimitry Polivaev
 */
class ReducedAttributeTableModelDecorator extends AttributeTableModelDecoratorAdapter {
	private Vector index = null;
	private int visibleRowCount;

	ReducedAttributeTableModelDecorator(final AttributeView attrView) {
		super(attrView);
		rebuildTableModel();
	}

	/*
	 * (non-Javadoc)
	 * @seefreemind.modes.attributes.AttributeTableModel#addRow(freemind.modes.
	 * attributes.Attribute)
	 */
	public void addRow(final Attribute newAttribute) {
		throw new Error();
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

	private int calcRow(final int row) {
		return ((Integer) index.get(row)).intValue();
	}

	private Vector getIndex() {
		if (index == null && attributeRegistry.getVisibleElementsNumber() > 0) {
			index = new Vector(nodeAttributeModel.getRowCount(), 10);
		}
		return index;
	}

	public int getRowCount() {
		return visibleRowCount;
	}

	public Object getValueAt(final int row, final int col) {
		return nodeAttributeModel.getValueAt(calcRow(row), col);
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.modes.attributes.AttributeTableModel#insertRow(int,
	 * freemind.modes.attributes.Attribute)
	 */
	public void insertRow(final int index, final Attribute newAttribute) {
		throw new Error();
	}

	@Override
	public boolean isCellEditable(final int row, final int col) {
		if (nodeAttributeModel.isCellEditable(row, col)) {
			return col == 1;
		}
		return false;
	}

	private void rebuildTableModel() {
		getIndex();
		if (index != null) {
			visibleRowCount = 0;
			index.clear();
			for (int i = 0; i < nodeAttributeModel.getRowCount(); i++) {
				final String name = (String) nodeAttributeModel.getValueAt(i, 0);
				if (attributeRegistry.getElement(name).isVisible()) {
					index.add(new Integer(i));
					visibleRowCount++;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.modes.attributes.AttributeTableModel#removeRow(int)
	 */
	public Object removeRow(final int index) {
		throw new Error();
	}

	@Override
	public void setValueAt(final Object o, final int row, final int col) {
		nodeAttributeModel.setValueAt(o, calcRow(row), col);
		fireTableCellUpdated(row, col);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
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
			fireTableDataChanged();
		}
	}
}
