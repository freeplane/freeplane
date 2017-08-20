package org.freeplane.plugin.collaboration.client.event.children;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.SingleNodeStructureManipulator;
import org.freeplane.plugin.collaboration.client.TestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChildrenUpdateProcessorSpec {
	@Mock
	private MapModel map;
	
	@Mock
	private SingleNodeStructureManipulator manipulator;
	
	@Mock
	private NodeFactory nodeFactory;

	@Test
	public void insertsNewNode() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(TestData.PARENT_NODE_ID);
		
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList(TestData.CHILD_NODE_ID)).build();
		
		NodeModel child = new NodeModel(map);
		when(nodeFactory.createNode(map, TestData.CHILD_NODE_ID)).thenReturn(child);

		new ChildrenUpdateProcessor(manipulator, map, nodeFactory).onMapUpdated(event);
		
		verify(manipulator).insertNode(child, parent, 0, false);
	}

	@Test
	public void insertsNewNodes() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(TestData.PARENT_NODE_ID);
		
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList(TestData.CHILD_NODE_ID, TestData.CHILD_NODE_ID2)).build();
		
		NodeModel child = new NodeModel(map);
		NodeModel child2 = new NodeModel(map);
		when(nodeFactory.createNode(map, TestData.CHILD_NODE_ID)).thenReturn(child);
		when(nodeFactory.createNode(map, TestData.CHILD_NODE_ID2)).thenReturn(child2);

		new ChildrenUpdateProcessor(manipulator, map, nodeFactory).onMapUpdated(event);
		
		verify(manipulator).insertNode(child, parent, 0, false);
		verify(manipulator).insertNode(child2, parent, 1, false);
		
		verifyNoMoreInteractions(manipulator);
	}
	
	@Test
	public void movesExistingNode() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(TestData.PARENT_NODE_ID);
		
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList(TestData.CHILD_NODE_ID)).build();
		
		NodeModel child = new NodeModel(map);
		when(map.getNodeForID( TestData.CHILD_NODE_ID)).thenReturn(child);
		parent.insert(child);

		new ChildrenUpdateProcessor(manipulator, map, nodeFactory).onMapUpdated(event);
		
		verify(manipulator).moveNode(child, parent, 0, false, false);
		verifyNoMoreInteractions(manipulator, nodeFactory);
	}
	
	@Test
	public void removeExistingNode() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(TestData.PARENT_NODE_ID);
		NodeModel child = new NodeModel(map);
		when(map.getNodeForID( TestData.CHILD_NODE_ID)).thenReturn(child);
		
		parent.insert(child);
		
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(Collections.<String>emptyList()).build();
		

		new ChildrenUpdateProcessor(manipulator, map, nodeFactory).onMapUpdated(event);
		
		verify(manipulator).deleteNode(parent, 0);
		verifyNoMoreInteractions(manipulator, nodeFactory);
	}
}
