/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2018 dimitry
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
package org.freeplane.plugin.collaboration.client.event.children;

import org.freeplane.collaboration.event.children.ImmutableNodePosition;
import org.freeplane.collaboration.event.children.NodeInserted;
import org.freeplane.collaboration.event.children.NodeMoved;
import org.freeplane.collaboration.event.children.NodePosition;
import org.freeplane.collaboration.event.children.NodeRemoved;
import org.freeplane.collaboration.event.children.RootNodeIdUpdated;
import org.freeplane.collaboration.event.children.Side;
import org.freeplane.collaboration.event.children.SpecialNodeTypeSet;
import org.freeplane.collaboration.event.children.SpecialNodeTypeSet.SpecialNodeType;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * Jan 6, 2018
 */
class MapStructureEventFactory {
	NodeInserted createNodeInsertedEvent(final NodeModel node) {
		return NodeInserted.builder()
		    .nodeId(node.createID())
		    .position(nodePositionOf(node)).build();
	}

	RootNodeIdUpdated createRootNodeIdUpdatedEvent(MapModel map) {
		return RootNodeIdUpdated.builder().nodeId(map.getRootNode().getID()).build();
	}

	NodeRemoved createNodeRemovedEvent(NodeModel child) {
		return NodeRemoved.builder().nodeId(child.createID()).build();
	}

	NodeMoved createNodeMovedEvent(NodeModel node) {
		return NodeMoved.builder()
		    .nodeId(node.createID())
		    .position(nodePositionOf(node)).build();
	}

	SpecialNodeTypeSet createSpecialNodeTypeSetEvent(final NodeModel node, SpecialNodeType c) {
		return SpecialNodeTypeSet.builder().nodeId(node.createID()).content(c).build();
	}

	private NodePosition nodePositionOf(NodeModel node) {
		NodeModel parent = node.getParentNode();
		ImmutableNodePosition.Builder builder = NodePosition.builder()
		    .parentId(parent.createID())
		    .position(node.getIndex());
		if (parent.isRoot())
			builder.side(sideOf(node));
		ImmutableNodePosition position = builder.build();
		return position;
	}

	private Side sideOf(NodeModel child) {
		return child.isLeft() ? Side.LEFT : Side.RIGHT;
	}
}
