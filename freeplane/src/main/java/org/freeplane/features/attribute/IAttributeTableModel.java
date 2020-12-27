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

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;

/**
 * @author Dimitry Polivaev
 */
public interface IAttributeTableModel {
	Quantity<LengthUnit> getColumnWidth(int col);

	int getRowCount();

	Object getValueAt(int row, int col);

	Attribute getAttribute(int row);
}
