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
import org.junit.Before;
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

	private NodeModel parent;

	private NodeModel child;

	private NodeModel child2;
	
	@Before
	public void setup() {
		parent = new NodeModel(map);
		parent.setText("parent");
		parent.setID(TestData.PARENT_NODE_ID);
		child = new NodeModel(map);
		child.setText("child");
		child2 = new NodeModel(map);
		child2.setText("child2");

	}

	@Test
	public void insertsRightNodeWithoutSideChange() throws Exception {
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList(TestData.CHILD_NODE_ID)).build();
		
		when(nodeFactory.createNode(map)).thenReturn(child);

		new ChildrenUpdateProcessor(manipulator, nodeFactory).onUpdate(map, event);
		
		verify(manipulator).insertNode(child, parent, 0, false);
	}

	@Test
	public void insertsRightNodeWithSideChange() throws Exception {
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList("RIGHT", TestData.CHILD_NODE_ID)).build();
		
		child.setLeft(true);
		
		when(nodeFactory.createNode(map)).thenReturn(child);

		new ChildrenUpdateProcessor(manipulator, nodeFactory).onUpdate(map, event);
		
		verify(manipulator).insertNode(child, parent, 0, false);
	}


	@Test
	public void insertsLeftNodeWithoutSideChange() throws Exception {
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList(TestData.CHILD_NODE_ID)).build();
		
		parent.setLeft(true);
		when(nodeFactory.createNode(map)).thenReturn(child);

		new ChildrenUpdateProcessor(manipulator, nodeFactory).onUpdate(map, event);
		
		verify(manipulator).insertNode(child, parent, 0, true);
	}


	@Test
	public void setsNewNodeId() throws Exception {
		
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList(TestData.CHILD_NODE_ID)).build();
		
		when(nodeFactory.createNode(map)).thenReturn(child);

		new ChildrenUpdateProcessor(manipulator, nodeFactory).onUpdate(map, event);
		
		assertThat(child.getID()).isEqualTo(TestData.CHILD_NODE_ID);
	}
	@Test
	public void insertsNewNodes() throws Exception {
		
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList(TestData.CHILD_NODE_ID, TestData.CHILD_NODE_ID2)).build();
		
		when(nodeFactory.createNode(map)).thenReturn(child).thenReturn(child2);

		new ChildrenUpdateProcessor(manipulator, nodeFactory).onUpdate(map, event);
		
		verify(manipulator).insertNode(child, parent, 0, false);
		verify(manipulator).insertNode(child2, parent, 1, false);
		
		verifyNoMoreInteractions(manipulator);
	}
	
	@Test
	public void movesExistingNodeToParentSide() throws Exception {
		
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList(TestData.CHILD_NODE_ID)).build();
		
		when(map.getNodeForID( TestData.CHILD_NODE_ID)).thenReturn(child);
		parent.insert(child);

		new ChildrenUpdateProcessor(manipulator, nodeFactory).onUpdate(map, event);
		
		verify(manipulator).moveNode(child, parent, 0, false, false);
		verifyNoMoreInteractions(manipulator, nodeFactory);
	}
	
	@Test
	public void movesExistingNodeToTheLeftParentSide() throws Exception {
		
		when(map.getNodeForID(TestData.PARENT_NODE_ID)).thenReturn(parent);

		ChildrenUpdated event = ImmutableChildrenUpdated.builder()
				.nodeId(TestData.PARENT_NODE_ID).
				content(asList("LEFT", TestData.CHILD_NODE_ID)).build();
		
		when(map.getNodeForID( TestData.CHILD_NODE_ID)).thenReturn(child);
		parent.insert(child);

		new ChildrenUpdateProcessor(manipulator, nodeFactory).onUpdate(map, event);
		
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
				content(Collections.<String>emptyList()).build();
		

		new ChildrenUpdateProcessor(manipulator, nodeFactory).onUpdate(map, event);
		
		verify(manipulator).deleteNode(parent, 0);
		verifyNoMoreInteractions(manipulator, nodeFactory);
	}
}
