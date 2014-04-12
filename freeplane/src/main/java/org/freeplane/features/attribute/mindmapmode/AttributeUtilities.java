/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Stefan Ott in 2011.
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
package org.freeplane.features.attribute.mindmapmode;

import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;

/**
 * This class has methods to get informations
 * about attributes from a NodeModel
 * 
 * @author Stefan Ott
 */
@EnabledAction(checkOnNodeChange = true)
public class AttributeUtilities {

	public AttributeUtilities() {
	};

	/**
	 * @return : the number of attributes attached to the node. 0 for none.
	 */
	public int getNumberOfAttributes(final NodeModel node) {
		if (hasAttributes(node)) {
			final NodeAttributeTableModel natm = NodeAttributeTableModel.getModel(node);
			return natm.getRowCount();
		}
		else {
			return 0;
		}
	}

	/**
	 * @return : true if the node has at least one attribute attached.
	 */
	public boolean hasAttributes(final NodeModel node) {
		final NodeAttributeTableModel natm = NodeAttributeTableModel.getModel(node);
		if (natm.getRowCount() > 0) {
			return true;
		}
		else {
			return false;
		}
	}
}
