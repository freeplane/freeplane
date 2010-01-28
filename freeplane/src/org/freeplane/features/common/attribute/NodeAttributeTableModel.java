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
package org.freeplane.features.common.attribute;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.icon.UIIcon;
import org.freeplane.core.icon.factory.IconStoreFactory;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.model.ITooltipProvider;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
public class NodeAttributeTableModel implements IExtension, IAttributeTableModel, TableModel {
	private static final String ATTRIBUTE_TOOLTIP = "attribute_tooltip";
	private static final int CAPACITY_INCREMENT = 10;
	public static final NodeAttributeTableModel EMTPY_ATTRIBUTES = new NodeAttributeTableModel(null);
	static private UIIcon attributeIcon = null;
	private static final String STATE_ICON = "AttributeExist";

	public static NodeAttributeTableModel getModel(final NodeModel node) {
		final NodeAttributeTableModel attributes = (NodeAttributeTableModel) node
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
		AttributeRegistry.getRegistry(node.getMap()).registry(newAttribute);
		attributes.add(newAttribute);
		setStateIcon();
		fireTableRowsInserted(index, index);
	}

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
		if (listeners == null) {
			return;
		}
		fireTableChanged(new TableModelEvent(this, row, row, column));
	}

	private void fireTableChanged(final TableModelEvent e) {
		if (listeners == null) {
			return;
		}
		for (final TableModelListener listener : listeners) {
			listener.tableChanged(e);
		}
	}

	public void fireTableRowsDeleted(final int firstRow, final int lastRow) {
		if (listeners == null) {
			return;
		}
		fireTableChanged(new TableModelEvent(this, firstRow, lastRow, TableModelEvent.ALL_COLUMNS,
		    TableModelEvent.DELETE));
	}

	public void fireTableRowsInserted(final int firstRow, final int lastRow) {
		if (listeners == null) {
			return;
		}
		fireTableChanged(new TableModelEvent(this, firstRow, lastRow, TableModelEvent.ALL_COLUMNS,
		    TableModelEvent.INSERT));
	}

	public void fireTableRowsUpdated(final int firstRow, final int lastRow) {
		if (listeners == null) {
			return;
		}
		fireTableChanged(new TableModelEvent(this, firstRow, lastRow, TableModelEvent.ALL_COLUMNS,
		    TableModelEvent.UPDATE));
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.modes.attributes.AttributeTableModel#get(int)
	 */
	public Attribute getAttribute(final int row) {
		return (Attribute) attributes.get(row);
	}

	public List<String> getAttributeKeyList() {
		final Vector<String> returnValue = new Vector<String>();
		for(Attribute attr : getAttributes()) {
			returnValue.add(attr.getName());
		}
		return returnValue;
	}

	public int getAttributePosition(final String pKey) {
		if (pKey == null) {
			return -1;
		}
		int pos = 0;
		for(Attribute attr : getAttributes()) {
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

	public Class getColumnClass(final int col) {
		return Object.class;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return 2;
	}

	public String getColumnName(final int col) {
		return "";
	}

	public int getColumnWidth(final int col) {
		return getLayout().getColumnWidth(col);
	}

	public AttributeTableLayoutModel getLayout() {
		if (layout == null) {
			layout = new AttributeTableLayoutModel();
		}
		return layout;
	}

	public Object getName(final int row) {
		final Attribute attr = (Attribute) attributes.get(row);
		return attr.getName();
	}

	public NodeModel getNode() {
		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return attributes == null ? 0 : attributes.size();
	}

	public Object getValue(final int row) {
		final Attribute attr = (Attribute) attributes.get(row);
		return attr.getValue();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
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

	private XMLElement initializeNodeAttributeLayoutXMLElement(XMLElement attributeElement) {
		if (attributeElement == null) {
			attributeElement = new XMLElement();
			attributeElement.setName(AttributeBuilder.XML_NODE_ATTRIBUTE_LAYOUT);
		}
		return attributeElement;
	}

	public boolean isCellEditable(final int arg0, final int arg1) {
		return false;
	}

	public void removeTableModelListener(final TableModelListener listener) {
		if (listeners == null) {
			return;
		}
		listeners.remove(listener);
	}

	void save(final ITreeWriter writer) throws IOException {
		saveLayout(writer);
		if (attributes != null) {
			for (int i = 0; i < attributes.size(); i++) {
				saveAttribute(writer, i);
			}
		}
	}

	private void saveAttribute(final ITreeWriter writer, final int i) throws IOException {
		final XMLElement attributeElement = new XMLElement();
		attributeElement.setName(AttributeBuilder.XML_NODE_ATTRIBUTE);
		final Attribute attr = (Attribute) attributes.get(i);
		attributeElement.setAttribute("NAME", attr.getName());
		attributeElement.setAttribute("VALUE", attr.getValue());
		writer.addElement(attr, attributeElement);
	}

	private void saveLayout(final ITreeWriter writer) throws IOException {
		if (layout != null) {
			XMLElement attributeElement = null;
			if (layout.getColumnWidth(0) != AttributeTableLayoutModel.DEFAULT_COLUMN_WIDTH) {
				attributeElement = initializeNodeAttributeLayoutXMLElement(attributeElement);
				attributeElement.setAttribute("NAME_WIDTH", Integer.toString(getColumnWidth(0)));
			}
			if (layout.getColumnWidth(1) != AttributeTableLayoutModel.DEFAULT_COLUMN_WIDTH) {
				attributeElement = initializeNodeAttributeLayoutXMLElement(attributeElement);
				attributeElement.setAttribute("VALUE_WIDTH", Integer.toString(layout.getColumnWidth(1)));
			}
			if (attributeElement != null) {
				writer.addElement(layout, attributeElement);
			}
		}
	}

	public void setName(final int row, final Object newName) {
		final Attribute attr = (Attribute) attributes.get(row);
		attr.setName(newName.toString());
		fireTableRowsUpdated(row, row);
	}

	public void setStateIcon() {
		final boolean showIcon = ResourceController.getResourceController().getBooleanProperty(
		    "show_icon_for_attributes");
		if (showIcon && getRowCount() == 0) {
			node.removeStateIcons(NodeAttributeTableModel.STATE_ICON);
		}
		if (showIcon && getRowCount() == 1) {
			if (NodeAttributeTableModel.attributeIcon == null) {
				NodeAttributeTableModel.attributeIcon = IconStoreFactory.create().getUIIcon("showAttributes.png");
			}
			node.setStateIcon(NodeAttributeTableModel.STATE_ICON, NodeAttributeTableModel.attributeIcon, true);
		}
		setTooltip();
	}

	protected void setTooltip() {
		final int rowCount = getRowCount();
		if (rowCount == 0) {
			node.setToolTip(ATTRIBUTE_TOOLTIP, null);
			return;
		}
		if (rowCount == 1) {
			node.setToolTip(ATTRIBUTE_TOOLTIP, new ITooltipProvider() {
				public String getTooltip() {
					final AttributeRegistry registry = AttributeRegistry.getRegistry(node.getMap());
					if (registry.getAttributeViewType().equals(AttributeTableLayoutModel.SHOW_ALL)) {
						return null;
					}
					final StringBuilder tooltip = new StringBuilder();
					tooltip.append("<html><body><table  border=\"1\">");
					final int currentRowCount = getRowCount();
					for (int i = 0; i < currentRowCount; i++) {
						tooltip.append("<tr><td>");
						tooltip.append(getValueAt(i, 0));
						tooltip.append("</td><td>");
						tooltip.append(getValueAt(i, 1));
						tooltip.append("</td></tr>");
					}
					tooltip.append("</table></body></html>");
					return tooltip.toString();
				}
			});
		}
	}

	public void setValue(final int row, final Object newValue) {
		final Attribute attr = (Attribute) attributes.get(row);
		attr.setValue(newValue.toString());
		fireTableRowsUpdated(row, row);
	}

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
