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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 */
public class NodeAttributeTableModel implements IExtension, IAttributeTableModel, TableModel {
	private static final int CAPACITY_INCREMENT = 10;
	public static final NodeAttributeTableModel EMTPY_ATTRIBUTES = new NodeAttributeTableModel(null);

	public static NodeAttributeTableModel getModel(final NodeModel node) {
		final NodeAttributeTableModel attributes = node
		    .getExtension(NodeAttributeTableModel.class);
		return attributes != null ? attributes : NodeAttributeTableModel.EMTPY_ATTRIBUTES;
	}

	private Vector<Attribute> attributes;
	private AttributeTableLayoutModel layout;
	private HashSet<TableModelListener> listeners;
	final private NodeModel node;

	public NodeAttributeTableModel(final NodeModel node) {
		this(node, 0);
	}

	public NodeAttributeTableModel(final NodeModel node, final int size) {
		super();
		allocateAttributes(size);
		this.node = node;
	}

	public void addRowNoUndo(final Attribute newAttribute) {
		allocateAttributes(NodeAttributeTableModel.CAPACITY_INCREMENT);
		final int index = getRowCount();
		final AttributeRegistry registry = AttributeRegistry.getRegistry(node.getMap());
		registry.registry(newAttribute);
		attributes.add(newAttribute);
		fireTableRowsInserted(index, index);
	}

	@Override
	public void addTableModelListener(final TableModelListener listener) {
		if (listeners == null) {
			listeners = new HashSet<TableModelListener>();
		}
		listeners.add(listener);
	}

	private void allocateAttributes(final int size) {
		if (attributes == null && size > 0) {
			attributes = new Vector<Attribute>(size, NodeAttributeTableModel.CAPACITY_INCREMENT);
		}
	}

	public void fireTableCellUpdated(final int row, final int column) {
		fireTableChanged(new TableModelEvent(this, row, row, column));
	}

	private void fireTableChanged(final TableModelEvent e) {
		node.getMap().getNodeChangeAnnouncer().nodeChanged(node, NodeAttributeTableModel.class, null, null);
		if (listeners != null) {
			final ArrayList<TableModelListener> arrayList = new ArrayList<TableModelListener>(listeners);
			for (final TableModelListener listener : arrayList) {
				listener.tableChanged(e);
			}
		}
	}

	public void fireTableRowsDeleted(final int firstRow, final int lastRow) {
		fireTableChanged(new TableModelEvent(this, firstRow, lastRow, TableModelEvent.ALL_COLUMNS,
		    TableModelEvent.DELETE));
	}

	public void fireTableRowsInserted(final int firstRow, final int lastRow) {
		fireTableChanged(new TableModelEvent(this, firstRow, lastRow, TableModelEvent.ALL_COLUMNS,
		    TableModelEvent.INSERT));
	}

	public void fireTableRowsUpdated(final int firstRow, final int lastRow) {
		fireTableChanged(new TableModelEvent(this, firstRow, lastRow, TableModelEvent.ALL_COLUMNS,
		    TableModelEvent.UPDATE));
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.modes.attributes.AttributeTableModel#get(int)
	 */
	public Attribute getAttribute(final int row) {
		return attributes.get(row);
	}

	public List<String> getAttributeKeyList() {
		final Vector<String> returnValue = new Vector<String>();
		for (final Attribute attr : getAttributes()) {
			returnValue.add(attr.getName());
		}
		return returnValue;
	}

	public int getAttributePosition(final String pKey) {
		if (pKey == null) {
			return -1;
		}
		int pos = 0;
		for (final Attribute attr : getAttributes()) {
			if (pKey.equals(attr.getName())) {
				return pos;
			}
			pos++;
		}
		return -1;
	}

	/**
	 * @return a list of Attribute elements.
	 */
	public Vector<Attribute> getAttributes() {
		allocateAttributes(NodeAttributeTableModel.CAPACITY_INCREMENT);
		return attributes;
	}

	public int getAttributeTableLength() {
		return getRowCount();
	}

	@Override
	public Class<Object> getColumnClass(final int col) {
		return Object.class;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(final int col) {
		return " ";
	}

	@Override
	public Quantity<LengthUnits> getColumnWidth(final int col) {
		return getLayout().getColumnWidth(col);
	}

	public AttributeTableLayoutModel getLayout() {
		if (layout == null) {
			layout = new AttributeTableLayoutModel();
		}
		return layout;
	}

	public Object getName(final int row) {
		final Attribute attr = attributes.get(row);
		return attr.getName();
	}

	@Override
	public NodeModel getNode() {
		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return attributes == null ? 0 : attributes.size();
	}

	public Object getValue(final int row) {
		final Attribute attr = attributes.get(row);
		return attr.getValue();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(final int row, final int col) {
		if (attributes != null) {
			switch (col) {
				case 0:
					return getName(row);
				case 1:
					return getValue(row);
			}
		}
		return null;
	}

	@Override
	public boolean isCellEditable(final int arg0, final int arg1) {
		return false;
	}

	@Override
	public void removeTableModelListener(final TableModelListener listener) {
		if (listeners == null) {
			return;
		}
		listeners.remove(listener);
	}

	public void setName(final int row, final Object newName) {
		final Attribute attr = attributes.get(row);
		attr.setName(newName.toString());
		fireTableRowsUpdated(row, row);
	}

	public void setValue(final int row, final Object newValue) {
		final Attribute attr = attributes.get(row);
		attr.setValue(newValue);
		fireTableRowsUpdated(row, row);
	}

	@Override
	public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
		switch (columnIndex) {
			case 0:
				setName(rowIndex, value);
				return;
			case 1:
				setValue(rowIndex, value);
				return;
			default:
				throw new ArrayIndexOutOfBoundsException(columnIndex + " >= 2");
		}
	}
}
