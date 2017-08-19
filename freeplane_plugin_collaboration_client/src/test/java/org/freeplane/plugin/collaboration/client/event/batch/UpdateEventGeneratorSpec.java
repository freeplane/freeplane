package org.freeplane.plugin.collaboration.client.event.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.plugin.collaboration.client.TestData;
import org.freeplane.plugin.collaboration.client.event.batch.UpdateEventGenerator;
import org.freeplane.plugin.collaboration.client.event.batch.UpdatesFinished;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdated;
import org.freeplane.plugin.collaboration.client.event.children.UpdateEventFactory;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateEventGeneratorSpec {
	
	private static final int DELAY_MILLIS = 10;

	@Mock
	private MapModel map;
	
	@Mock
	private UpdateEventFactory eventFactory;

	@BeforeClass
	static public void setup() throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable() {
			
			@Override
			public void run() {
			}
		});
	}
	
	@Test
	public void generatesEventOnNodeInsertion() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(TestData.PARENT_NODE_ID);
		final ChildrenUpdated childrenUpdated = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);

		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		
		UpdateEventGenerator uut = new UpdateEventGenerator(consumer, eventFactory, DELAY_MILLIS);
		uut.onNodeInserted(parent, null, 0);
		
		final UpdatesFinished event = consumer.getEvent(DELAY_MILLIS + 50, TimeUnit.MILLISECONDS);
		assertThat(event.updateEvents()).containsExactly(childrenUpdated);
	}

	@Test
	public void generatesOneUpdateEventPerParent() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(TestData.PARENT_NODE_ID);
		final ChildrenUpdated childrenUpdated = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);

		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		
		UpdateEventGenerator uut = new UpdateEventGenerator(consumer, eventFactory, DELAY_MILLIS);
		
		uut.onNodeInserted(parent, null, 0);
		uut.onNodeInserted(parent, null, 0);
		
		Thread.sleep(DELAY_MILLIS + 50);
		final UpdatesFinished event = consumer.getEvent(0, TimeUnit.MILLISECONDS);
		assertThat(event.updateEvents()).containsExactly(childrenUpdated);
	}
	
	@Test
	public void generatesEventOnNodeInsertionAfterDelay() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(TestData.PARENT_NODE_ID);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		UpdateEventGenerator uut = new UpdateEventGenerator(consumer, eventFactory, DELAY_MILLIS);
		uut.onNodeInserted(parent, null, 0);
		
		verifyZeroInteractions(eventFactory);
	}

	@Test
	public void generatesMultipleEventsOnNodeInsertionToDifferentParents() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(TestData.PARENT_NODE_ID);
		final NodeModel parent2 = new NodeModel(map);
		parent2.setID(TestData.PARENT_NODE_ID2);
		
		final ChildrenUpdated childrenUpdated = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);

		final ChildrenUpdated childrenUpdated2 = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent2)).thenReturn(childrenUpdated2);

		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		UpdateEventGenerator uut = new UpdateEventGenerator(consumer, eventFactory, DELAY_MILLIS);
		uut.onNodeInserted(parent, null, 0);
		uut.onNodeInserted(parent2, null, 0);
		final UpdatesFinished event = consumer.getEvent(DELAY_MILLIS + 100, TimeUnit.MILLISECONDS);
		assertThat(event.updateEvents()).containsExactly(childrenUpdated, childrenUpdated2);
	}


	@Test
	public void generatesMultipleBatchesOnNodeInsertionToDifferentParentsWithPause() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(TestData.PARENT_NODE_ID);
		final NodeModel parent2 = new NodeModel(map);
		parent2.setID(TestData.PARENT_NODE_ID2);
		
		final ChildrenUpdated childrenUpdated = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);

		final ChildrenUpdated childrenUpdated2 = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent2)).thenReturn(childrenUpdated2);

		UpdatesEventCaptor consumer = new UpdatesEventCaptor(2);
		UpdateEventGenerator uut = new UpdateEventGenerator(consumer, eventFactory, DELAY_MILLIS);
		uut.onNodeInserted(parent, null, 0);
		Thread.sleep(DELAY_MILLIS + 100);
		uut.onNodeInserted(parent2, null, 0);
		final List<UpdatesFinished> events = consumer.getEvents(DELAY_MILLIS + 100, TimeUnit.MILLISECONDS);
		assertThat(events).hasSize(2);
		assertThat(events.get(0).updateEvents()).containsExactly(childrenUpdated);
		assertThat(events.get(1).updateEvents()).containsExactly(childrenUpdated2);
	}

	
	@Test
	public void generatesEventOnNodeDeletion() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(TestData.PARENT_NODE_ID);
		final ChildrenUpdated childrenUpdated = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);

		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		
		UpdateEventGenerator uut = new UpdateEventGenerator(consumer, eventFactory, DELAY_MILLIS);
		uut.onNodeDeleted(new NodeDeletionEvent(parent, null, 0));
		
		final UpdatesFinished event = consumer.getEvent(DELAY_MILLIS + 50, TimeUnit.MILLISECONDS);
		assertThat(event.updateEvents()).containsExactly(childrenUpdated);
	}

	
	@Test
	public void generatesEventOnNodeMove() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(TestData.PARENT_NODE_ID);
		final NodeModel parent2 = new NodeModel(map);
		parent2.setID(TestData.PARENT_NODE_ID2);
		
		final ChildrenUpdated childrenUpdated = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);

		final ChildrenUpdated childrenUpdated2 = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent2)).thenReturn(childrenUpdated2);

		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		UpdateEventGenerator uut = new UpdateEventGenerator(consumer, eventFactory, DELAY_MILLIS);
		uut.onNodeMoved(new NodeMoveEvent(parent, 0, false, parent2, null, 0, false));
		final UpdatesFinished event = consumer.getEvent(DELAY_MILLIS + 100, TimeUnit.MILLISECONDS);
		assertThat(event.updateEvents()).containsExactly(childrenUpdated, childrenUpdated2);
	}

	@Ignore("todo")
	@Test
	public void generatesSpecialNodeTypeEventOnNodeInsertion() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(TestData.PARENT_NODE_ID);
		final ChildrenUpdated childrenUpdated = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);

		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		
		UpdateEventGenerator uut = new UpdateEventGenerator(consumer, eventFactory, DELAY_MILLIS);
		uut.onNodeInserted(parent, null, 0);
		
		final UpdatesFinished event = consumer.getEvent(DELAY_MILLIS + 50, TimeUnit.MILLISECONDS);
		assertThat(event.updateEvents()).containsExactly(childrenUpdated);
	}
}
