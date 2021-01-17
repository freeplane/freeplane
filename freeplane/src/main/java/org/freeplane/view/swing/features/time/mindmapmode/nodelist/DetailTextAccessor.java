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

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.DetailModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;

/**
 * @author  Dimitry Polivaev 03.10.2013
 */
class DetailTextAccessor implements TextAccessor {
	final private NodeModel node;

	public DetailTextAccessor(NodeModel node) {
		this.node = node;
	}

	public String getText() {
	    String details = DetailModel.getDetailText(node);
		return details != null ? details : "";
	}

	public void setText(String newText) {
		((MTextController) TextController.getController()).setDetails(node, newText);
    }

	public NodeModel getNode() {
	    return node;
    }
}