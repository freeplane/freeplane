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

import org.freeplane.collaboration.event.children.NodeMoved;
import org.freeplane.collaboration.event.children.NodePosition;
import org.freeplane.collaboration.event.children.Side;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.SingleNodeStructureManipulator;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;

/**
 * @author Dimitry Polivaev
 * Jan 5, 2018
 */
public class NodeMovedProcessor implements UpdateProcessor<NodeMoved>{
	private SingleNodeStructureManipulator manipulator;

	public NodeMovedProcessor(SingleNodeStructureManipulator manipulator) {
		this.manipulator = manipulator;
	}

	@Override
	public void onUpdate(MapModel map, NodeMoved event) {
		final NodePosition position = event.position();
		NodeModel parent = map.getNodeForID(position.parentId());
		NodeModel child = map.getNodeForID(event.nodeId());
		boolean isLeft = position.side().map(Side::isLeft).orElse(parent.isLeft());
		manipulator.moveNode(child, parent, position.position(), isLeft, isLeft != child.isLeft());
	}

	@Override
	public Class<NodeMoved> eventClass() {
		return NodeMoved.class;
	}
}
