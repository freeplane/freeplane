package org.freeplane.plugin.collaboration.client.event.batch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.plugin.collaboration.client.TestData;
import org.freeplane.plugin.collaboration.client.event.children.UpdateEventFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateEventGeneratorSpec {
	@Mock
	private MapModel map;
	
	@Mock
	private UpdateEventFactory eventFactory;
	
	@Mock
	private MapUpdateTimerFactory updateTimerFactory;

	@Mock
	private MapUpdateTimer updateTimer;
	
	private NodeModel createNode(MapModel map, String nodeId) {
		NodeModel node = new NodeModel(map);
		node.setID(nodeId);
		return node;
	}
	
	@Test
	public void generatesEventOnNodeInsertion() throws Exception {
		final NodeModel parent = createNode(map, TestData.PARENT_NODE_ID);
		final NodeModel child = createNode(map, TestData.CHILD_NODE_ID);
		when(updateTimerFactory.create(map)).thenReturn(updateTimer);
		UpdateEventGenerator uut = new UpdateEventGenerator(updateTimerFactory);

		uut.onNodeInserted(parent, child, 0);
		
		verify(updateTimer).onNodeInserted(parent, child);
	}

	@Test
	public void generatesEventOnNodeDeletion() throws Exception {
		final NodeModel parent = createNode(map, TestData.PARENT_NODE_ID);
		final NodeModel child = createNode(map, TestData.CHILD_NODE_ID);
		when(updateTimerFactory.create(map)).thenReturn(updateTimer);
		UpdateEventGenerator uut = new UpdateEventGenerator(updateTimerFactory);

		uut.onNodeDeleted(new NodeDeletionEvent(parent, child, 0));
		
		verify(updateTimer).onChangedStructure(parent);
	}
	
	@Test
	public void generatesEventOnNodeMove() throws Exception {
		final NodeModel parent = createNode(map, TestData.PARENT_NODE_ID);
		final NodeModel parent2 = createNode(map, TestData.PARENT_NODE_ID2);
		when(updateTimerFactory.create(map)).thenReturn(updateTimer);
		UpdateEventGenerator uut = new UpdateEventGenerator(updateTimerFactory);

		uut.onNodeMoved(new NodeMoveEvent(parent, 0, false, parent2, null, 0, false));
		
		verify(updateTimer).onChangedStructure(parent);
	}

	@Test
	public void generatesEventsOnNodeInsertionToDifferentMaps() throws Exception {
		final NodeModel parent = createNode(map, TestData.PARENT_NODE_ID);
		final NodeModel child = createNode(map, TestData.CHILD_NODE_ID);
		
		MapModel map2 = mock(MapModel.class);
		final NodeModel parent2 = createNode(map2, TestData.PARENT_NODE_ID2);
		final NodeModel child2 = createNode(map2, TestData.CHILD_NODE_ID2);
		
		MapUpdateTimer updateTimer2 = mock(MapUpdateTimer.class);
		
		when(updateTimerFactory.create(map)).thenReturn(updateTimer);
		when(updateTimerFactory.create(map2)).thenReturn(updateTimer2);
		
		UpdateEventGenerator uut = new UpdateEventGenerator(updateTimerFactory);

		uut.onNodeInserted(parent, child, 0);
		uut.onNodeInserted(parent2, child2, 0);
		
		verify(updateTimer).onNodeInserted(parent, child);
		verify(updateTimer2).onNodeInserted(parent2, child2);
	}
}
