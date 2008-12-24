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
package org.freeplane.controller.filter.util;

import javax.swing.AbstractListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
class DoubleListModel extends AbstractListModel implements IListModel {
	final private IListModel first;
	final private IListModel second;

	public DoubleListModel(final IListModel first, final IListModel second) {
		super();
		this.first = first;
		this.second = second;
		first.addListDataListener(new ListDataListener() {
			public void contentsChanged(final ListDataEvent e) {
				fireContentsChanged(e.getSource(), e.getIndex0(), e.getIndex1());
			}

			public void intervalAdded(final ListDataEvent e) {
				fireIntervalAdded(e.getSource(), e.getIndex0(), e.getIndex1());
			}

			public void intervalRemoved(final ListDataEvent e) {
				fireIntervalRemoved(e.getSource(), e.getIndex0(), e.getIndex1());
			}
		});
		second.addListDataListener(new ListDataListener() {
			public void contentsChanged(final ListDataEvent e) {
				final int firstSize = first.getSize();
				fireContentsChanged(e.getSource(), firstSize + e.getIndex0(), firstSize
				        + e.getIndex1());
			}

			public void intervalAdded(final ListDataEvent e) {
				final int firstSize = first.getSize();
				fireIntervalAdded(e.getSource(), firstSize + e.getIndex0(), firstSize
				        + e.getIndex1());
			}

			public void intervalRemoved(final ListDataEvent e) {
				final int firstSize = first.getSize();
				fireIntervalRemoved(e.getSource(), firstSize + e.getIndex0(), firstSize
				        + e.getIndex1());
			}
		});
	}

	public void add(final Object o) {
		if (!first.contains(o)) {
			second.add(o);
		}
	}

	public void clear() {
		first.clear();
		second.clear();
	}

	public boolean contains(final Object o) {
		return first.contains(o) || second.contains(o);
	}

	@Override
	protected void fireContentsChanged(final Object source, final int index0, final int index1) {
		// TODO Auto-generated method stub
		super.fireContentsChanged(source, index0, index1);
	}

	@Override
	protected void fireIntervalAdded(final Object source, final int index0, final int index1) {
		// TODO Auto-generated method stub
		super.fireIntervalAdded(source, index0, index1);
	}

	@Override
	protected void fireIntervalRemoved(final Object source, final int index0, final int index1) {
		// TODO Auto-generated method stub
		super.fireIntervalRemoved(source, index0, index1);
	}

	public Object getElementAt(final int index) {
		final int firstSize = first.getSize();
		return index < firstSize ? first.getElementAt(index) : second.getElementAt(index
		        - firstSize);
	}

	public int getIndexOf(final Object o) {
		final int index = first.getIndexOf(o);
		if (index != -1) {
			return index;
		}
		return second.getIndexOf(o) + first.getSize();
	}

	public int getSize() {
		return first.getSize() + second.getSize();
	}

	public void remove(final Object o) {
		first.remove(o);
		second.remove(o);
	}

	public void replace(final Object oldO, final Object newO) {
		if (first.contains(oldO)) {
			first.replace(oldO, newO);
		}
		else {
			second.replace(oldO, newO);
		}
	}
}
