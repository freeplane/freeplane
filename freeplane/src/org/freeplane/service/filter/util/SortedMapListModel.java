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
package org.freeplane.service.filter.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractListModel;

public class SortedMapListModel extends AbstractListModel implements
        ISortedListModel {
	SortedSet model;

	public SortedMapListModel() {
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
		final Iterator i = iterator();
		int count = -1;
		while (i.hasNext()) {
			count++;
			if (i.next().equals(o)) {
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
	 * freemind.controller.filter.util.SortedListModel#delete(java.lang.Object)
	 */
	public void remove(final Object element) {
		if (model.remove(element)) {
			fireContentsChanged(this, 0, getSize());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.controller.filter.util.SortedListModel#replace(java.lang.Object,
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
}
