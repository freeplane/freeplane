/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2017 dimitry
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
package org.freeplane.plugin.collaboration.client.event;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.TestData;
import org.mockito.Mockito;

/**
 * @author Dimitry Polivaev
 * Nov 25, 2017
 */
public class TestObjects {
	public final MapModel map;
	public final NodeModel parent;

	public final NodeModel child;

	public final NodeModel parent2;

	public final NodeModel child2;

	public TestObjects() {
		map = Mockito.mock(MapModel.class);
		parent = createNode(TestData.PARENT_NODE_ID, "parent");
		child = createNode(TestData.CHILD_NODE_ID, "child");
		
		parent2 = createNode(TestData.PARENT_NODE_ID2, "parent2");
		child2 = createNode(TestData.CHILD_NODE_ID2, "child2");
	}


	public NodeModel createNode(MapModel map, String nodeId, String text) {
		NodeModel node = new NodeModel(map);
		node.setID(nodeId);
		node.setText(text);
		return node;
	}
	
	private NodeModel createNode(String id, String text) {
		final NodeModel node = createNode(map, id, text);
		Mockito.when(map.getNodeForID(id)).thenReturn(node);
		return node;
	}
	
}