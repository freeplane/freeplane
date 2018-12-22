/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.view.swing.map.attribute;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.net.URI;

import javax.swing.Icon;

/**
 * @author Dimitry Polivaev
 * Mar 4, 2011
 */
class CursorUpdater extends MouseAdapter implements MouseMotionListener{

	@Override
	public void mouseDragged(MouseEvent e) {
    }

	@Override
	public void mouseMoved(MouseEvent e) {
		updateCursor(e);
    }

	@Override
    public void mouseEntered(MouseEvent e) {
		updateCursor(e);
    }

	@Override
    public void mouseExited(MouseEvent e) {
		updateCursor(e.getComponent(), Cursor.DEFAULT_CURSOR);
    }


	private void updateCursor(MouseEvent e) {
	    final int cursor = getCursor(e);
	    updateCursor(e.getComponent(), cursor);

    }

	private int getCursor(MouseEvent e) {
		final AttributeTable table = (AttributeTable) e.getComponent();
		final Point point = e.getPoint();
		final int col = table.columnAtPoint(point);
		if(col != 1){
			return Cursor.DEFAULT_CURSOR;
		}
		final int row = table.rowAtPoint(e.getPoint());
        if(row == -1  || row >= table.getRowCount()){
            return Cursor.DEFAULT_CURSOR;
        }
		Object value = table.getValueAt(row, col);
		URI uri = table.toUri(value);
		if(uri == null){
			return Cursor.DEFAULT_CURSOR;
		}
		final Icon linkIcon = table.getLinkIcon(uri);
		if (linkIcon == null)
			return Cursor.DEFAULT_CURSOR;
		final int leftColumnWidth = table.getColumnModel().getColumn(0).getWidth();
		if (point.x < leftColumnWidth + linkIcon.getIconWidth()) {
			return Cursor.HAND_CURSOR;
		}
		return Cursor.DEFAULT_CURSOR;
    }

	private void updateCursor(Component component, int cursor) {
		final Cursor newCursor = Cursor.getPredefinedCursor(cursor);
		if( component.getCursor().equals(newCursor))
			return;
		component.setCursor(cursor == Cursor.DEFAULT_CURSOR ? null : newCursor);
    }

}
