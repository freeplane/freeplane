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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 */
public class NodeAttributeTableModel implements IExtension, IAttributeTableModel {
	private static final DefaultTableModel DUMMY = new DefaultTableModel();
	private static final int CAPACITY_INCREMENT = 10;
	public static final NodeAttributeTableModel EMTPY_ATTRIBUTES = new NodeAttributeTableModel();

	public static NodeAttributeTableModel getModel(final NodeModel node) {
		final NodeAttributeTableModel attributes = node
		    .getExtension(NodeAttributeTableModel.class);
		return attributes != null ? attributes : NodeAttributeTableModel.EMTPY_ATTRIBUTES;
	}

	private Vector<Attribute> attributes;
	private AttributeTableLayoutModel layout;
	private Set<TableModelListener> listeners;

	public NodeAttributeTableModel() {
		this(0);
	}

	public NodeAttributeTableModel(final int size) {
		super();
		allocateAttributes(size);
	}

	public void addRowNoUndo(NodeModel node, final Attribute newAttribute) {
		allocateAttributes(NodeAttributeTableModel.CAPACITY_INCREMENT);
		final int index = getRowCount();
		final AttributeRegistry registry = AttributeRegistry.getRegistry(node.getMap());
		registry.registry(newAttribute);
		attributes.add(newAttribute);
		fireTableRowsInserted(node, index, index);
	}

	public void addTableModelListener(final TableModelListener listener) {
		if (listeners == null) {
			listeners = new LinkedHashSet<TableModelListener>();
		}
		listeners.add(listener);
	}

	private void allocateAttributes(final int size) {
		if (attributes == null && size > 0) {
			attributes = new Vector<Attribute>(size, NodeAttributeTableModel.CAPACITY_INCREMENT);
		}
	}

	public void fireTableCellUpdated(NodeModel node, final int row, final int column) {
		fireTableChanged(node, new TableModelEvent(DUMMY, row, row, column));
	}

	private void fireTableChanged(NodeModel node, final TableModelEvent e) {
		node.getMap().getNodeChangeAnnouncer().nodeChanged(node, NodeAttributeTableModel.class, null, null);
		if (listeners != null) {
			final ArrayList<TableModelListener> arrayList = new ArrayList<TableModelListener>(listeners);
			for (final TableModelListener listener : arrayList) {
				listener.tableChanged(e);
			}
		}
	}

	public void fireTableRowsDeleted(NodeModel node, final int firstRow, final int lastRow) {
		fireTableChanged(node, new TableModelEvent(DUMMY, firstRow, lastRow, TableModelEvent.ALL_COLUMNS,
		    TableModelEvent.DELETE));
	}

	public void fireTableRowsInserted(NodeModel node, final int firstRow, final int lastRow) {
		fireTableChanged(node, new TableModelEvent(DUMMY, firstRow, lastRow, TableModelEvent.ALL_COLUMNS,
		    TableModelEvent.INSERT));
	}

	public void fireTableRowsUpdated(NodeModel node, final int firstRow, final int lastRow) {
		fireTableChanged(node, new TableModelEvent(DUMMY, firstRow, lastRow, TableModelEvent.ALL_COLUMNS,
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

	public int getAttributeIndex(final String name) {
		if (name == null) {
			return -1;
		}
		int pos = 0;
		for (final Attribute attr : getAttributes()) {
			if (name.equals(attr.getName())) {
				return pos;
			}
			pos++;
		}
		return -1;
	}

	public int getAttributeIndex(final Attribute searchedAttribute) {
		int pos = 0;
		for (final Attribute attribute : getAttributes()) {
			if (attribute == searchedAttribute) {
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
	public Quantity<LengthUnit> getColumnWidth(final int col) {
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

	public void removeTableModelListener(final TableModelListener listener) {
		if (listeners == null) {
			return;
		}
		listeners.remove(listener);
	}

	public void setName(NodeModel node, final int row, final Object newName) {
		final Attribute attr = attributes.get(row);
		attr.setName(newName.toString());
		fireTableRowsUpdated(node, row, row);
	}

	public void setValue(NodeModel node, final int row, final Object newValue) {
		final Attribute attr = attributes.get(row);
		attr.setValue(newValue);
		fireTableRowsUpdated(node, row, row);
	}

	public void setValueAt(NodeModel node, final Object value, final int rowIndex, final int columnIndex) {
		switch (columnIndex) {
			case 0:
				setName(node, rowIndex, value);
				return;
			case 1:
				setValue(node, rowIndex, value);
				return;
			default:
				throw new ArrayIndexOutOfBoundsException(columnIndex + " >= 2");
		}
	}

}
