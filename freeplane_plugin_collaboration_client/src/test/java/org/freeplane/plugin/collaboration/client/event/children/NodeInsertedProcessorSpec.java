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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.SingleNodeStructureManipulator;
import org.freeplane.plugin.collaboration.client.event.TestObjects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Dimitry Polivaev
 * Jan 6, 2018
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeInsertedProcessorSpec {
	@Mock
	private SingleNodeStructureManipulator manipulator;
	@Mock
	private NodeFactory nodeFactory;
	@InjectMocks
	private NodeInsertedProcessor uut;
	
	MapStructureEventFactory factory = new MapStructureEventFactory();
	
	final private TestObjects testObjects = new TestObjects();
	final private MapModel map = testObjects.map;
	final private NodeModel parent = testObjects.parent;
	final private NodeModel child = testObjects.child;
	final private NodeModel child2 = testObjects.child2;
	
	@Test
	public void eventClass() throws Exception {
		assertThat(uut.eventClass()).isEqualTo(NodeInserted.class);
	}

	@Test
	public void insertsRightFirstLevelNode() throws Exception {
		parent.insert(child);
		when(map.getRootNode()).thenReturn(parent);
		when(nodeFactory.createNode(map)).thenReturn(child2);
		uut.onUpdate(map, factory.createNodeInsertedEvent(child));
		verify(manipulator).insertNode(child2, parent, 0, false);
	}
	
	@Test
	public void insertsLeftFirstLevelNode() throws Exception {
		child.setLeft(true);
		parent.insert(child);
		when(map.getRootNode()).thenReturn(parent);
		when(nodeFactory.createNode(map)).thenReturn(child2);
		uut.onUpdate(map, factory.createNodeInsertedEvent(child));
		verify(manipulator).insertNode(child2, parent, 0, true);
	}

	@Test
	public void insertsRightNonFirstLevelNode() throws Exception {
		parent.insert(child);
		when(nodeFactory.createNode(map)).thenReturn(child2);
		uut.onUpdate(map, factory.createNodeInsertedEvent(child));
		verify(manipulator).insertNode(child2, parent, 0, false);
	}
	
	@Test
	public void insertsLeftNonFirstLevelNode() throws Exception {
		parent.setLeft(true);
		parent.insert(child);
		when(nodeFactory.createNode(map)).thenReturn(child2);
		uut.onUpdate(map, factory.createNodeInsertedEvent(child));
		verify(manipulator).insertNode(child2, parent, 0, true);
	}
}
