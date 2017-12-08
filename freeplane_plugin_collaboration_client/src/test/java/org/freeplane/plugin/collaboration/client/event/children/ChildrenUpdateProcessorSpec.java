package org.freeplane.plugin.collaboration.client.event.children;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.SingleNodeStructureManipulator;
import org.freeplane.plugin.collaboration.client.TestData;
import org.freeplane.plugin.collaboration.client.event.TestObjects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChildrenUpdateProcessorSpec {
	final private TestObjects testObjects = new TestObjects();
	final private MapModel map = testObjects.map;
	final private NodeModel parent = testObjects.parent;
	final private NodeModel child = testObjects.child;
	final private NodeModel child2 = testObjects.child2;
	
	@Mock
	private SingleNodeStructureManipulator manipulator;
	
	@Mock
	private NodeFactory nodeFactory;

	@InjectMocks
	private ChildrenUpdateProcessor uut;
	
	@Test
	public void returnsEventClass_ChildrenUpdated() throws Exception {
		assertThat(uut.eventClass()).isEqualTo(ChildrenUpdated.class);
	}
	
	@Test
	public void insertsRightNodeWithoutSideChange() throws Exception {
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList(TestData.CHILD)).build();
		
		when(nodeFactory.createNode(map)).thenReturn(child);

		uut.onUpdate(map, event);
		
		verify(manipulator).insertNode(child, parent, 0, false);
	}

	@Test
	public void insertsRightNodeWithSideChange() throws Exception {
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList(TestData.RIGHT_CHILD)).build();
		
		child.setLeft(true);
		
		when(nodeFactory.createNode(map)).thenReturn(child);

		uut.onUpdate(map, event);
		
		verify(manipulator).insertNode(child, parent, 0, false);
	}


	@Test
	public void insertsLeftNodeWithoutSideChange() throws Exception {
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList(TestData.CHILD)).build();
		
		parent.setLeft(true);
		when(nodeFactory.createNode(map)).thenReturn(child);

		uut.onUpdate(map, event);
		
		verify(manipulator).insertNode(child, parent, 0, true);
	}


	@Test
	public void setsNewNodeId() throws Exception {
		
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList(TestData.CHILD)).build();
		
		when(nodeFactory.createNode(map)).thenReturn(child);

		uut.onUpdate(map, event);
		
		assertThat(child.getID()).isEqualTo(TestData.CHILD_NODE_ID);
	}
	@Test
	public void insertsNewNodes() throws Exception {
		
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList(TestData.CHILD, TestData.CHILD2)).build();
		
		when(nodeFactory.createNode(map)).thenReturn(child).thenReturn(child2);

		uut.onUpdate(map, event);
		
		verify(manipulator).insertNode(child, parent, 0, false);
		verify(manipulator).insertNode(child2, parent, 1, false);
		
		verifyNoMoreInteractions(manipulator);
	}
	
	@Test
	public void movesExistingNodeToParentSide() throws Exception {
		
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList(TestData.CHILD)).build();
		
		when(map.getNodeForID( TestData.CHILD_NODE_ID)).thenReturn(child);
		parent.insert(child);

		uut.onUpdate(map, event);
		
		verify(manipulator).moveNode(child, parent, 0, false, false);
		verifyNoMoreInteractions(manipulator, nodeFactory);
	}
	
	@Test
	public void movesExistingNodeToTheLeftParentSide() throws Exception {
		
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList(TestData.LEFT_CHILD)).build();
		
		when(map.getNodeForID( TestData.CHILD_NODE_ID)).thenReturn(child);
		parent.insert(child);

		uut.onUpdate(map, event);
		
		verify(manipulator).moveNode(child, parent, 0, true, true);
		verifyNoMoreInteractions(manipulator, nodeFactory);
	}
	@Test
	public void removeExistingNode() throws Exception {
		NodeModel child = new NodeModel(map);
		when(map.getNodeForID( TestData.CHILD_NODE_ID)).thenReturn(child);
		
		parent.insert(child);
		
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(Collections.emptyList()).build();
		

		uut.onUpdate(map, event);
		
		verify(manipulator).deleteNode(parent, 0);
		verifyNoMoreInteractions(manipulator, nodeFactory);
	}
}
