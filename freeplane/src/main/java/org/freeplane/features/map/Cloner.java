/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 Dimitry
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
package org.freeplane.features.map;

import java.util.List;

import org.freeplane.features.map.NodeModel.CloneType;

/**
 * @author Dimitry Polivaev
 * 09.02.2014
 */
class Cloner {
	
	private final NodeModel nodeModel;

	public Cloner(NodeModel nodeModel) {
		this.nodeModel = nodeModel;
    }

    public NodeModel cloneTree() {
		final NodeModel clone = cloneStructure(nodeModel);
		return clone;
	}

	private NodeModel cloneStructure(NodeModel node) {
		if(node.containsExtension(EncryptionModel.class))
			throw new CloneEncryptedNodeException();
		final NodeModel clone = node.cloneNode(CloneType.TREE);
		final List<NodeModel> cloneChildren = clone.getChildrenInternal();
		for (NodeModel childNode : node.getChildrenInternal()){
			final NodeModel childClone = cloneStructure(childNode);
			childClone.setParent(clone);
			cloneChildren.add(childClone);
		}
		return clone;
    }
}
