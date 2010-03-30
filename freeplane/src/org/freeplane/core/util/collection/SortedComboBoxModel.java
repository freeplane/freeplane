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
package org.freeplane.core.util.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 * @author Dimitry Polivaev
 */
public class SortedComboBoxModel extends AbstractListModel implements ComboBoxModel, IListModel, Iterable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object selectedItem;
	private final SortedSet model;

	public SortedComboBoxModel() {
		model = new TreeSet();
	}

	public void add(final Object element) {
		if (model.add(element)) {
			fireContentsChanged(this, 0, getSize());
		}
	}

	public void addAll(final Object elements[]) {
		final Collection c = Arrays.asList(elements);
		model.addAll(c);
		fireContentsChanged(this, 0, getSize());
	}

	public void clear() {
		final int oldSize = getSize();
		if (oldSize > 0) {
			model.clear();
			fireIntervalRemoved(this, 0, oldSize - 1);
		}
	}

	public boolean contains(final Object element) {
		return model.contains(element);
	}

	public Object firstElement() {
		return model.first();
	}

	public Object getElementAt(final int index) {
		return model.toArray()[index];
	}

	/**
	*/
	public int getIndexOf(final Object o) {
		int count = -1;
		for (final Object element : this) {
			count++;
			if (element.equals(o)) {
				return count;
			}
		}
		return -1;
	}

	public int getSize() {
		return model.size();
	}

	public Iterator iterator() {
		return model.iterator();
	}

	public Object lastElement() {
		return model.last();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.util.SortedListModel#delete(java.lang.Object)
	 */
	public void remove(final Object element) {
		if (model.remove(element)) {
			fireContentsChanged(this, 0, getSize());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.util.SortedListModel#replace(java.lang.Object,
	 * java.lang.Object)
	 */
	public void replace(final Object oldO, final Object newO) {
		if (oldO.equals(newO)) {
			return;
		}
		final boolean removed = model.remove(oldO);
		final boolean added = model.add(newO);
		if (removed || added) {
			fireContentsChanged(this, 0, getSize());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.ComboBoxModel#getSelectedItem()
	 */
	public Object getSelectedItem() {
		return selectedItem;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
	 */
	public void setSelectedItem(final Object o) {
		selectedItem = o;
		fireContentsChanged(this, -1, -1);
	}
}
