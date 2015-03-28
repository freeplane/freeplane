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

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 * @author Dimitry Polivaev
 */
public class SortedComboBoxModel extends AbstractListModel implements ComboBoxModel, IListModel, Iterable<Object> {
	static private class Comparator implements Comparable<Object>{
		final private Object obj;
		private Comparator(Object obj) {
	        this.obj = obj;
        }
		public int compareTo(Object o) {
			return compareTo((Comparator)o);
		}
        private int compareTo(Comparator o) {
			final int stringCompare = obj.toString().compareTo(o.obj.toString());
			if(stringCompare != 0)
				return stringCompare;
			final int typeCompare = obj.getClass().getName().compareTo(o.obj.getClass().getName());
			return typeCompare;
        }
		@Override
        public int hashCode() {
	        return obj.hashCode();
        }
		
		@Override
        public boolean equals(Object o) {
			return obj.getClass().equals(o.getClass()) && obj.equals(((Comparator)o).obj);
        }
		@Override
        public String toString() {
	        return obj.toString();
        }
		
	}
	private static final long serialVersionUID = 1L;
	private Object selectedItem;
	private final SortedMap<Comparator, Object> model;

	public SortedComboBoxModel() {
		model = new TreeMap<Comparator, Object>();
	}

	public void add(final Object element) {
		if(addImpl(element))
			fireContentsChanged(this, 0, getSize());
	}

	private boolean addImpl(final Object element) {
	    final Comparator key = key(element);
		if(model.containsKey(key))
			return false;
		model.put(key, element);
		return true;
    }

	private Comparator key(Object o){
		return new Comparator(o);		
	}
	public void addAll(final Object elements[]) {
		for(Object e : elements)
			addImpl(e);
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
		return model.containsKey(key(element));
	}

	public Object firstElement() {
		return model.get(model.firstKey());
	}

	public Object getElementAt(final int index) {
		return model.values().toArray()[index];
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

	public Iterator<Object> iterator() {
		return model.values().iterator();
	}

	public Object lastElement() {
		return model.get(model.lastKey());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.util.SortedListModel#delete(java.lang.Object)
	 */
	public void remove(final Object element) {
		if (null != model.remove(key(element))) {
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
		final boolean removed = null != model.remove(key(oldO));
		final boolean added = addImpl(newO);
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
