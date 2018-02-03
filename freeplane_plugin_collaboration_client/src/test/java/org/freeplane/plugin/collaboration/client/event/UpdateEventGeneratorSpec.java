package org.freeplane.plugin.collaboration.client.event;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.plugin.collaboration.client.event.batch.UpdateBlockGeneratorFactory;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;
import org.freeplane.plugin.collaboration.client.event.children.MapStructureEventGenerator;
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
	private MapStructureEventGenerator updateGenerator;

	private ContentUpdateGenerators contentUpdateGenerators;

	@Mock
	private ContentUpdateGenerator contentUpdateGenerator;

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
		uut = new UpdateEventGenerator(updateGenerator, contentUpdateGenerators);
	}
	
	@Test
	public void generatesEventOnNodeInsertion() throws Exception {
		when(updateTimerFactory.of(map)).thenReturn(updateTimer);

		uut.onNodeInserted(parent, child, 0);
		
		verify(updateGenerator).onNodeInserted(child);
	}

	@Test
	public void generatesEventOnNodeDeletion() throws Exception {
		when(updateTimerFactory.of(map)).thenReturn(updateTimer);

		uut.onNodeDeleted(new NodeDeletionEvent(parent, child, 0));
		
		verify(updateGenerator).onNodeRemoved(child);
	}
	
	@Test
	public void generatesEventOnNodeMove() throws Exception {
		when(updateTimerFactory.of(map)).thenReturn(updateTimer);
		uut.onNodeMoved(new NodeMoveEvent(parent, 0, false, parent2, child, 0, false));
		verify(updateGenerator).onNodeMoved(child);
	}

	@Test
	public void generatesEventOnNodeChange() throws Exception {
		NodeChangeEvent event = new NodeChangeEvent(parent, NodeModel.UNKNOWN_PROPERTY, null, null, true);
		when(contentUpdateGenerator.handles(event)).thenReturn(true);
		uut.nodeChanged(event);
		verify(contentUpdateGenerator).onNodeChange(event);
	}

	@Test
	public void generatesNoEventOnNonPersistentNodeChange() throws Exception {
		NodeChangeEvent event = new NodeChangeEvent(parent, NodeModel.UNKNOWN_PROPERTY, null, null, false);
		uut.nodeChanged(event);
		verifyZeroInteractions(contentUpdateGenerator);
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
