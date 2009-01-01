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
package org.freeplane.map.attribute;

import org.freeplane.core.map.NodeModel;

public interface IAttributeController {
	void performInsertRow(NodeAttributeTableModel model, int index, String name, String value);

	void performRegistryAttribute(String name);

	void performRegistryAttributeValue(String name, String value);

	void performRegistrySubtreeAttributes(NodeModel child);

	void performRemoveAttribute(String name);

	void performRemoveAttributeValue(String name, String value);

	void performRemoveRow(NodeAttributeTableModel model, int index);

	void performReplaceAtributeName(String oldName, String newName);

	void performReplaceAttributeValue(String name, String oldValue, String newValue);

	void performSetColumnWidth(NodeAttributeTableModel model, int col, int width);

	void performSetFontSize(AttributeRegistry registry, int size);

	void performSetRestriction(int i, boolean b);

	void performSetValueAt(NodeAttributeTableModel model, Object o, int row, int col);

	void performSetVisibility(int i, boolean b);
}
