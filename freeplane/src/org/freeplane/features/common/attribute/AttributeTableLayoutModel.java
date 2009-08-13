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

import javax.swing.event.EventListenerList;

/**
 * @author Dimitry Polivaev
 */
public class AttributeTableLayoutModel {
	public static final int DEFAULT_COLUMN_WIDTH = 75;
	public static final String HIDE_ALL = "hide";
	public static final String SHOW_ALL = "extended";
	public static final String SHOW_SELECTED = "selected";
	ColumnWidthChangeEvent[] layoutChangeEvent = { null, null };
	private EventListenerList listenerList = null;
	final private int[] width = { AttributeTableLayoutModel.DEFAULT_COLUMN_WIDTH,
	        AttributeTableLayoutModel.DEFAULT_COLUMN_WIDTH };

	public AttributeTableLayoutModel() {
		super();
	}

	public void addColumnWidthChangeListener(final IColumnWidthChangeListener l) {
		getListenerList().add(IColumnWidthChangeListener.class, l);
	}

	protected void fireColumnWidthChanged(final int col) {
		final Object[] listeners = getListenerList().getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IColumnWidthChangeListener.class) {
				if (layoutChangeEvent[col] == null) {
					layoutChangeEvent[col] = new ColumnWidthChangeEvent(this, col);
				}
				((IColumnWidthChangeListener) listeners[i + 1]).columnWidthChanged(layoutChangeEvent[col]);
			}
		}
	}

	public int getColumnWidth(final int col) {
		return width[col];
	}

	/**
	 * @return Returns the listenerList.
	 */
	private EventListenerList getListenerList() {
		if (listenerList == null) {
			listenerList = new EventListenerList();
		}
		return listenerList;
	}

	public void removeColumnWidthChangeListener(final IColumnWidthChangeListener l) {
		getListenerList().remove(IColumnWidthChangeListener.class, l);
	}

	public void setColumnWidth(final int col, final int width) {
		if (this.width[col] != width) {
			this.width[col] = width;
			fireColumnWidthChanged(col);
		}
	}
}
