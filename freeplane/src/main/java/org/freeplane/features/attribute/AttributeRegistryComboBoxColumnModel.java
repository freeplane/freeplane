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

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.freeplane.core.util.collection.IListModel;

class AttributeRegistryComboBoxColumnModel extends AbstractListModel implements TableModelListener, ComboBoxModel,
        IListModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private AttributeRegistry model;
	private Object selectedItem;

	public AttributeRegistryComboBoxColumnModel(final AttributeRegistry model) {
		super();
		this.model = model;
		model.getTableModel().addTableModelListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.util.SortedListModel#add(java.lang.Object)
	 */
	public void add(final Object o) {
		final String s = o.toString();
		if (-1 == model.indexOf(s)) {
			model.getAttributeController().performRegistryAttributeValue(s, "", false);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.controller.filter.util.SortedListModel#clear()
	 */
	public void clear() {
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.util.SortedListModel#contains(java.lang.Object
	 * )
	 */
	public boolean contains(final Object o) {
		return model.containsElement(o.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public Object getElementAt(final int row) {
		return model.getKey(row);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.util.SortedListModel#getIndexOf(java.lang.
	 * Object)
	 */
	public int getIndexOf(final Object o) {
		return model.indexOf(o.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.ComboBoxModel#getSelectedItem()
	 */
	public Object getSelectedItem() {
		return selectedItem;
	}

	public int getSize() {
		return model.size();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.util.SortedListModel#delete(java.lang.Object)
	 */
	public void remove(final Object o) {
		model.removeAtribute(o);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.util.SortedListModel#replace(java.lang.Object,
	 * java.lang.Object)
	 */
	public void replace(final Object oldO, final Object newO) {
		model.getAttributeController().performReplaceAtributeName(oldO.toString(), newO.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
	 */
	public void setSelectedItem(final Object o) {
		selectedItem = o;
		fireContentsChanged(this, -1, -1);
	}

	/*
	 * (non-Javadoc)
	 * @seejavax.swing.event.TableModelListener#tableChanged(javax.swing.event.
	 * TableModelEvent)
	 */
	public void tableChanged(final TableModelEvent e) {
		if (e.getType() == TableModelEvent.DELETE) {
			fireIntervalRemoved(this, e.getFirstRow(), e.getLastRow());
			return;
		}
		if (e.getType() == TableModelEvent.UPDATE) {
			fireContentsChanged(this, e.getFirstRow(), e.getLastRow());
			return;
		}
		if (e.getType() == TableModelEvent.INSERT) {
			fireIntervalAdded(this, e.getFirstRow(), e.getLastRow());
			return;
		}
	}
}
