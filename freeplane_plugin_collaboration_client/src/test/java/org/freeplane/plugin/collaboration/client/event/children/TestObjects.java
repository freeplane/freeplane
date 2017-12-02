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
package org.freeplane.plugin.collaboration.client.event.children;

import static org.mockito.Mockito.mock;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.TestData;
import org.mockito.Mockito;

/**
 * @author Dimitry Polivaev
 * Nov 25, 2017
 */
class TestObjects {
	 final MapModel map;
	 final NodeModel parent;

	 final NodeModel child;

	 final ChildrenUpdated childrenUpdated;

	 final NodeModel parent2;

	 final NodeModel child2;

	 final ChildrenUpdated childrenUpdated2;
	 private int counter = 0;
	
	

	public TestObjects() {
		map = Mockito.mock(MapModel.class);
		parent = createNode(TestData.PARENT_NODE_ID);
		child = createNode(TestData.CHILD_NODE_ID);
		childrenUpdated = mock(ChildrenUpdated.class);
		
		parent2 = createNode(TestData.PARENT_NODE_ID2);
		child2 = createNode(TestData.CHILD_NODE_ID2);
		childrenUpdated2 = mock(ChildrenUpdated.class);
	}



	NodeModel createNode(MapModel map, String nodeId) {
		NodeModel node = new NodeModel(map);
		node.setID(nodeId);
		node.setText(Integer.toString(counter++));
		return node;
	}
	
	NodeModel createNode(String id) {
		return createNode(map, id);
	}
	
}