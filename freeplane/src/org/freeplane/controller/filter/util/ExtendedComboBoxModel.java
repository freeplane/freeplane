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

import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * @author Dimitry Polivaev
 */
public class ExtendedComboBoxModel extends DefaultComboBoxModel {
	private class ExtensionDataListener implements ListDataListener {
		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.ListDataListener#contentsChanged(javax.swing.event
		 * .ListDataEvent)
		 */
		public void contentsChanged(final ListDataEvent e) {
			final int size = getOwnSize();
			fireContentsChanged(getModel(), size + e.getIndex0(), size
			        + e.getIndex1());
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.ListDataListener#intervalAdded(javax.swing.event
		 * .ListDataEvent)
		 */
		public void intervalAdded(final ListDataEvent e) {
			final int size = getOwnSize();
			fireIntervalAdded(getModel(), size + e.getIndex0(), size
			        + e.getIndex1());
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event
		 * .ListDataEvent)
		 */
		public void intervalRemoved(final ListDataEvent e) {
			final int size = getOwnSize();
			fireIntervalRemoved(getModel(), size + e.getIndex0(), size
			        + e.getIndex1());
		}
	}

	private ISortedListModel extension = null;
	final private ExtensionDataListener extensionDataListener = new ExtensionDataListener();

	public ExtendedComboBoxModel() {
		super();
	}

	public ExtendedComboBoxModel(final Object[] o) {
		super(o);
	}

	public ExtendedComboBoxModel(final Vector v) {
		super(v);
	}

	public void addSortedElement(final Object o) {
		if (extension != null && !extension.contains(o)) {
			extension.add(o);
		}
	}

	@Override
	public Object getElementAt(final int i) {
		final int s = getOwnSize();
		if (i < s || extension == null) {
			return super.getElementAt(i);
		}
		return extension.getElementAt(i - s);
	}

	private int getExtensionSize() {
		return extension != null ? extension.getSize() : 0;
	}

	@Override
	public int getIndexOf(final Object o) {
		final int idx = super.getIndexOf(o);
		if (idx > -1 || extension == null) {
			return idx;
		}
		final int extIdx = extension.getIndexOf(o);
		return extIdx > -1 ? extIdx + getOwnSize() : -1;
	}

	private ExtendedComboBoxModel getModel() {
		return this;
	}

	/**
	 */
	private int getOwnSize() {
		return super.getSize();
	}

	@Override
	public int getSize() {
		return getOwnSize() + getExtensionSize();
	}

	@Override
	public void insertElementAt(final Object o, final int i) {
		super.insertElementAt(o, Math.min(getOwnSize(), i));
	}

	@Override
	public void removeAllElements() {
		super.removeAllElements();
		if (extension != null) {
			extension.clear();
		}
	}

	@Override
	public void removeElement(final Object o) {
		super.removeElement(o);
	}

	@Override
	public void removeElementAt(final int i) {
		if (i < getOwnSize()) {
			super.removeElementAt(i);
		}
	}

	/**
	 */
	public void setExtensionList(final ISortedListModel sortedListModel) {
		final int ownSize = getOwnSize();
		{
			if (extension != null) {
				extension.removeListDataListener(extensionDataListener);
				final int extensionSize = getExtensionSize();
				if (extensionSize > 0) {
					fireIntervalRemoved(this, ownSize, ownSize + extensionSize
					        - 1);
				}
			}
		}
		{
			extension = sortedListModel;
			final int extensionSize = getExtensionSize();
			if (extensionSize > 0) {
				fireIntervalAdded(this, ownSize, ownSize + extensionSize - 1);
			}
			if (extension != null) {
				extension.addListDataListener(extensionDataListener);
			}
		}
	}
}
