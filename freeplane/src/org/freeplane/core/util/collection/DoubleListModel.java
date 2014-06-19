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

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
public class DoubleListModel extends AbstractListModel implements ListModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private ListModel first;
	final private ListModel second;

	public DoubleListModel(final ListModel first, final ListModel second) {
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
				fireContentsChanged(e.getSource(), firstSize + e.getIndex0(), firstSize + e.getIndex1());
			}

			public void intervalAdded(final ListDataEvent e) {
				final int firstSize = first.getSize();
				fireIntervalAdded(e.getSource(), firstSize + e.getIndex0(), firstSize + e.getIndex1());
			}

			public void intervalRemoved(final ListDataEvent e) {
				final int firstSize = first.getSize();
				fireIntervalRemoved(e.getSource(), firstSize + e.getIndex0(), firstSize + e.getIndex1());
			}
		});
	}

	public Object getElementAt(final int index) {
		final int firstSize = first.getSize();
		return index < firstSize ? first.getElementAt(index) : second.getElementAt(index - firstSize);
	}

	public int getSize() {
		return first.getSize() + second.getSize();
	}
}
