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
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.TestObjects;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeSet.SpecialNodeType;
import org.junit.Test;

/**
 * @author Dimitry Polivaev
 * Jan 6, 2018
 */
public class MapStructureEventFactorySpec {
	final private TestObjects testObjects = new TestObjects();
	final private MapModel map = testObjects.map;
	final private NodeModel parent = testObjects.parent;
	final private NodeModel child = testObjects.child;
	final private NodeModel child2 = testObjects.child2;
	MapStructureEventFactory uut = new MapStructureEventFactory();
	
	@Test
	public void createsNodeInsertedEventForLeftFirstLevelNode() throws Exception {
		when(map.getRootNode()).thenReturn(parent);
		parent.insert(child);
		parent.insert(child2);
		child2.setLeft(true);
		
		NodePosition expectedPosition = NodePosition.builder()
				.parentId(parent.getID())
				.position(1)
				.side(Side.LEFT).build();
		NodeInserted expected = NodeInserted.builder()
		.nodeId(child2.getID())
		.position(expectedPosition).build();
		
		assertThat(uut.createNodeInsertedEvent(child2)).isEqualTo(expected);
	}
	
	@Test
	public void createsNodeMovedEventForRightFirstLevelNode() throws Exception {
		when(map.getRootNode()).thenReturn(parent);
		parent.insert(child);
		parent.insert(child2);
		child2.setLeft(false);
		
		NodePosition expectedPosition = NodePosition.builder()
				.parentId(parent.getID())
				.position(1)
				.side(Side.RIGHT).build();
		NodeInserted expected = NodeInserted.builder()
		.nodeId(child2.getID())
		.position(expectedPosition).build();
		
		assertThat(uut.createNodeInsertedEvent(child2)).isEqualTo(expected);
	}

	public void createsNodeInsertedEventForLeftNonFirstLevelNode() throws Exception {
		parent.insert(child);
		parent.insert(child2);
		child2.setLeft(true);
		
		NodePosition expectedPosition = NodePosition.builder()
				.parentId(parent.getID())
				.position(1)
				.build();
		NodeInserted expected = NodeInserted.builder()
		.nodeId(child2.getID())
		.position(expectedPosition).build();
		
		assertThat(uut.createNodeInsertedEvent(child2)).isEqualTo(expected);
	}
	

	public void createsNodeRemovedEvent() throws Exception {
		NodeRemoved expected = NodeRemoved.builder()
		.nodeId(child2.getID()).build();
		
		assertThat(uut.createNodeRemovedEvent(child2)).isEqualTo(expected);
	}
	
	public void createsSpecialNodeTypeSetEvent() throws Exception {
		SpecialNodeTypeSet expected = SpecialNodeTypeSet.builder().nodeId(child.createID()).content(SpecialNodeType.SUMMARY_BEGIN).build();
		
		assertThat(uut.createSpecialNodeTypeSetEvent(child, SpecialNodeType.SUMMARY_BEGIN)).isEqualTo(expected);
	}
	
	public void createsRootNodeIdUpdatedEvent() throws Exception {
		when(map.getRootNode()).thenReturn(parent);
		RootNodeIdUpdated expected = RootNodeIdUpdated.builder().nodeId(parent.getID()).build();
		
		assertThat(uut.createRootNodeIdUpdatedEvent(map)).isEqualTo(expected);
	}
	
}
