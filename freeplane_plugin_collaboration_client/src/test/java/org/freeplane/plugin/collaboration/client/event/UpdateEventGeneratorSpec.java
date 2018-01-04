package org.freeplane.plugin.collaboration.client.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.plugin.collaboration.client.TestData;
import org.freeplane.plugin.collaboration.client.event.batch.UpdateBlockGeneratorFactory;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdateGenerator;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdateGenerators;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateGenerators;
import org.freeplane.plugin.collaboration.client.event.content.other.ContentUpdateGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateEventGeneratorSpec {
	@Mock
	private UpdateBlockGeneratorFactory updateTimerFactory;

	@Mock
	private ChildrenUpdateGenerators childrenUpdateGenerators;

	private ContentUpdateGenerators contentUpdateGenerators;

	@Mock
	private ContentUpdateGenerator contentUpdateGenerator;
	@Mock
	private ChildrenUpdateGenerator updateGenerator;

	@Mock
	private Updates updateTimer;
	
	final private TestObjects testObjects = new TestObjects();
	final private MapModel map = testObjects.map;
	final private NodeModel parent = testObjects.parent;
	final private NodeModel child = testObjects.child;
	final private NodeModel parent2 = testObjects.parent2;

	private UpdateEventGenerator uut;


	@Before
	public void setup() {
		contentUpdateGenerators = new ContentUpdateGenerators(
			Arrays.asList(contentUpdateGenerator), 
			Arrays.asList(contentUpdateGenerator));
		uut = new UpdateEventGenerator(childrenUpdateGenerators, contentUpdateGenerators);
	}
	
	@Test
	public void generatesEventOnNodeInsertion() throws Exception {
		when(updateTimerFactory.of(map)).thenReturn(updateTimer);
		when(childrenUpdateGenerators.of(map)).thenReturn(updateGenerator);

		uut.onNodeInserted(parent, child, 0);
		
		verify(updateGenerator).onNodeInserted(parent, child);
	}

	@Test
	public void generatesEventOnNodeDeletion() throws Exception {
		when(updateTimerFactory.of(map)).thenReturn(updateTimer);
		when(childrenUpdateGenerators.of(map)).thenReturn(updateGenerator);

		uut.onNodeDeleted(new NodeDeletionEvent(parent, child, 0));
		
		verify(updateGenerator).onChangedStructure(parent);
	}
	
	@Test
	public void generatesEventOnNodeMove() throws Exception {
		when(updateTimerFactory.of(map)).thenReturn(updateTimer);
		when(childrenUpdateGenerators.of(map)).thenReturn(updateGenerator);

		uut.onNodeMoved(new NodeMoveEvent(parent, 0, false, parent2, null, 0, false));
		
		verify(updateGenerator).onChangedStructure(parent);
	}

	@Test
	public void generatesEventsOnNodeInsertionToDifferentMaps() throws Exception {
		MapModel map2 = mock(MapModel.class);
		final NodeModel parent2 = testObjects.createNode(map2, TestData.PARENT_NODE_ID2, "parent2");
		final NodeModel child2 = testObjects.createNode(map2, TestData.CHILD_NODE_ID2, "child2");
		
		Updates updateTimer2 = mock(Updates.class);
		ChildrenUpdateGenerator updateGenerator2 = mock(ChildrenUpdateGenerator.class);
		
		when(updateTimerFactory.of(map)).thenReturn(updateTimer);
		when(childrenUpdateGenerators.of(map)).thenReturn(updateGenerator);
		when(updateTimerFactory.of(map2)).thenReturn(updateTimer2);
		when(childrenUpdateGenerators.of(map2)).thenReturn(updateGenerator2);

		uut.onNodeInserted(parent, child, 0);
		uut.onNodeInserted(parent2, child2, 0);
		
		verify(updateGenerator).onNodeInserted(parent, child);
		verify(updateGenerator2).onNodeInserted(parent2, child2);
	}

	@Test
	public void generatesEventOnNodeChange() throws Exception {
		NodeChangeEvent event = new NodeChangeEvent(parent, NodeModel.UNKNOWN_PROPERTY, null, null);
		when(contentUpdateGenerator.handles(event)).thenReturn(true);
		uut.nodeChanged(event);
		verify(contentUpdateGenerator).onNodeChange(parent);
	}

	@Test
	public void generatesEventOnMapChange() throws Exception {
		when(map.getRootNode()).thenReturn(parent);
		MapChangeEvent event = new MapChangeEvent(this, map, NodeModel.UNKNOWN_PROPERTY, null, null);
		when(contentUpdateGenerator.handles(event)).thenReturn(true);
		uut.mapChanged(event);
		
		verify(contentUpdateGenerator).onMapChange(map);
	}
}
