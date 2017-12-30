package org.freeplane.plugin.collaboration.client.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.plugin.collaboration.client.TestData;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimer;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimerFactory;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdateGenerator;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdateGenerators;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateGenerator;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateGenerators;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateEventGeneratorSpec {
	@Mock
	private MapUpdateTimerFactory updateTimerFactory;

	@Mock
	private ChildrenUpdateGenerators childrenUpdateGenerators;

	@Mock
	private ContentUpdateGenerators contentUpdateGenerators;

	@Mock
	private ChildrenUpdateGenerator updateGenerator;

	@Mock
	private ContentUpdateGenerator contentUpdateGenerator;

	@Mock
	private MapUpdateTimer updateTimer;
	
	final private TestObjects testObjects = new TestObjects();
	final private MapModel map = testObjects.map;
	final private NodeModel parent = testObjects.parent;
	final private NodeModel child = testObjects.child;
	final private NodeModel parent2 = testObjects.parent2;


	@Test
	public void generatesEventOnNodeInsertion() throws Exception {
		when(updateTimerFactory.createTimer(map)).thenReturn(updateTimer);
		when(childrenUpdateGenerators.of(map)).thenReturn(updateGenerator);
		UpdateEventGenerator uut = new UpdateEventGenerator(childrenUpdateGenerators, contentUpdateGenerators);

		uut.onNodeInserted(parent, child, 0);
		
		verify(updateGenerator).onNodeInserted(parent, child);
	}

	@Test
	public void generatesEventOnNodeDeletion() throws Exception {
		when(updateTimerFactory.createTimer(map)).thenReturn(updateTimer);
		when(childrenUpdateGenerators.of(map)).thenReturn(updateGenerator);
		UpdateEventGenerator uut = new UpdateEventGenerator(childrenUpdateGenerators, contentUpdateGenerators);

		uut.onNodeDeleted(new NodeDeletionEvent(parent, child, 0));
		
		verify(updateGenerator).onChangedStructure(parent);
	}
	
	@Test
	public void generatesEventOnNodeMove() throws Exception {
		when(updateTimerFactory.createTimer(map)).thenReturn(updateTimer);
		when(childrenUpdateGenerators.of(map)).thenReturn(updateGenerator);
		UpdateEventGenerator uut = new UpdateEventGenerator(childrenUpdateGenerators, contentUpdateGenerators);

		uut.onNodeMoved(new NodeMoveEvent(parent, 0, false, parent2, null, 0, false));
		
		verify(updateGenerator).onChangedStructure(parent);
	}

	@Test
	public void generatesEventsOnNodeInsertionToDifferentMaps() throws Exception {
		MapModel map2 = mock(MapModel.class);
		final NodeModel parent2 = testObjects.createNode(map2, TestData.PARENT_NODE_ID2, "parent2");
		final NodeModel child2 = testObjects.createNode(map2, TestData.CHILD_NODE_ID2, "child2");
		
		MapUpdateTimer updateTimer2 = mock(MapUpdateTimer.class);
		ChildrenUpdateGenerator updateGenerator2 = mock(ChildrenUpdateGenerator.class);
		
		when(updateTimerFactory.createTimer(map)).thenReturn(updateTimer);
		when(childrenUpdateGenerators.of(map)).thenReturn(updateGenerator);
		when(updateTimerFactory.createTimer(map2)).thenReturn(updateTimer2);
		when(childrenUpdateGenerators.of(map2)).thenReturn(updateGenerator2);
		
		UpdateEventGenerator uut = new UpdateEventGenerator(childrenUpdateGenerators, contentUpdateGenerators);

		uut.onNodeInserted(parent, child, 0);
		uut.onNodeInserted(parent2, child2, 0);
		
		verify(updateGenerator).onNodeInserted(parent, child);
		verify(updateGenerator2).onNodeInserted(parent2, child2);
	}

	@Test
	public void generatesEventOnNodeChange() throws Exception {
		when(updateTimerFactory.createTimer(map)).thenReturn(updateTimer);
		when(contentUpdateGenerators.of(map)).thenReturn(contentUpdateGenerator);
		UpdateEventGenerator uut = new UpdateEventGenerator(childrenUpdateGenerators, contentUpdateGenerators);

		uut.nodeChanged(new NodeChangeEvent(parent, NodeModel.UNKNOWN_PROPERTY, null, null));
		
		verify(contentUpdateGenerator).onNodeContentUpdate(parent);
	}

	@Test
	public void generatesEventOnMapChange() throws Exception {
		when(updateTimerFactory.createTimer(map)).thenReturn(updateTimer);
		when(contentUpdateGenerators.of(map)).thenReturn(contentUpdateGenerator);
		when(map.getRootNode()).thenReturn(parent);
		UpdateEventGenerator uut = new UpdateEventGenerator(childrenUpdateGenerators, contentUpdateGenerators);

		uut.mapChanged(new MapChangeEvent(this, map, NodeModel.UNKNOWN_PROPERTY, null, null));
		
		verify(contentUpdateGenerator).onNodeContentUpdate(parent);
	}
}
