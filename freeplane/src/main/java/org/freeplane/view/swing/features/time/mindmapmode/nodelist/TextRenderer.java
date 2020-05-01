/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.view.swing.features.time.mindmapmode.nodelist;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author Dimitry Polivaev
 * 03.10.2013
 */
class TextRenderer extends DefaultTableCellRenderer {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public TextRenderer() {
		super();
	}

	@Override
	public void setValue(final Object value) {
		setText((value == null) ? "" : ((TextHolder) value).getTextAsSingleLine());
	}
}