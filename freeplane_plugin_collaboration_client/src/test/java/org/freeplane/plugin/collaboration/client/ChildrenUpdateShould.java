package org.freeplane.plugin.collaboration.client;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.SingleNodeStructureManipulator;
import org.freeplane.plugin.collaboration.client.UpdateSpecification.ContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChildrenUpdateShould {
	private static final String PARENT_NODE_ID = "id_parent";
	private static final String CHILD_NODE_ID = "id_child";
	private static final String CHILD_NODE_ID2 = "id_child2";

	@Mock
	private MapModel map;
	
	@Mock
	private SingleNodeStructureManipulator manipulator;
	
	@Mock
	private NodeFactory nodeFactory;

	@Test
	public void insertsNewNode() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(PARENT_NODE_ID);
		
		when(map.getNodeForID(PARENT_NODE_ID)).thenReturn(parent);

		UpdateSpecification specification = ImmutableUpdateSpecification.builder()
				.contentType(ContentType.CHILDREN)
				.nodeId(PARENT_NODE_ID).
				content(CHILD_NODE_ID).build();
		
		NodeModel child = new NodeModel(map);
		when(nodeFactory.createNode(map, CHILD_NODE_ID)).thenReturn(child);

		new ChildrenUpdate(manipulator, map, nodeFactory, specification).apply();
		
		verify(manipulator).insertNode(child, parent, 0, false);
	}

	@Test
	public void insertsNewNodes() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(PARENT_NODE_ID);
		
		when(map.getNodeForID(PARENT_NODE_ID)).thenReturn(parent);

		UpdateSpecification specification = ImmutableUpdateSpecification.builder()
				.contentType(ContentType.CHILDREN)
				.nodeId(PARENT_NODE_ID).
				content(CHILD_NODE_ID + ',' + CHILD_NODE_ID2).build();
		
		NodeModel child = new NodeModel(map);
		NodeModel child2 = new NodeModel(map);
		when(nodeFactory.createNode(map, CHILD_NODE_ID)).thenReturn(child);
		when(nodeFactory.createNode(map, CHILD_NODE_ID2)).thenReturn(child2);

		new ChildrenUpdate(manipulator, map, nodeFactory, specification).apply();
		
		verify(manipulator).insertNode(child, parent, 0, false);
		verify(manipulator).insertNode(child2, parent, 1, false);
		
		verifyNoMoreInteractions(manipulator);
	}
	
	@Test
	public void movesExistingNode() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(PARENT_NODE_ID);
		
		when(map.getNodeForID(PARENT_NODE_ID)).thenReturn(parent);

		UpdateSpecification specification = ImmutableUpdateSpecification.builder()
				.contentType(ContentType.CHILDREN)
				.nodeId(PARENT_NODE_ID).
				content(CHILD_NODE_ID).build();
		
		NodeModel child = new NodeModel(map);
		when(map.getNodeForID( CHILD_NODE_ID)).thenReturn(child);
		parent.insert(child);

		new ChildrenUpdate(manipulator, map, nodeFactory, specification).apply();
		
		verify(manipulator).moveNode(child, parent, 0, false, false);
		verifyNoMoreInteractions(manipulator, nodeFactory);
	}
	
	@Test
	public void removeExistingNode() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(PARENT_NODE_ID);
		NodeModel child = new NodeModel(map);
		when(map.getNodeForID( CHILD_NODE_ID)).thenReturn(child);
		
		parent.insert(child);
		
		when(map.getNodeForID(PARENT_NODE_ID)).thenReturn(parent);

		UpdateSpecification specification = ImmutableUpdateSpecification.builder()
				.contentType(ContentType.CHILDREN)
				.nodeId(PARENT_NODE_ID).
				content("").build();
		

		new ChildrenUpdate(manipulator, map, nodeFactory, specification).apply();
		
		verify(manipulator).deleteNode(parent, 0);
		verifyNoMoreInteractions(manipulator, nodeFactory);
	}
}
