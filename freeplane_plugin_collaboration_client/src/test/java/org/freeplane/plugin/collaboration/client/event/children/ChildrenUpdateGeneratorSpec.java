package org.freeplane.plugin.collaboration.client.event.children;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNodeFlag;
import org.freeplane.plugin.collaboration.client.TestData;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimer;
import org.freeplane.plugin.collaboration.client.event.batch.ModifiableUpdateHeaderExtension;
import org.freeplane.plugin.collaboration.client.event.batch.UpdatesFinished;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeSet.SpecialNodeType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChildrenUpdateGeneratorSpec {
	
	private static final int TIMEOUT = 100;

	private static final int DELAY_MILLIS = 10;
	

	
	@Mock
	private UpdateEventFactory eventFactory;
	
	private ModifiableUpdateHeaderExtension header = ModifiableUpdateHeaderExtension.create().setMapId("mapId").setMapRevision(0);

	private UpdatesEventCaptor consumer;

	private ChildrenUpdateGenerator uut;

	final private TestObjects testObjects = new TestObjects();
	final private MapModel map = testObjects.map;
	final private NodeModel parent = testObjects.parent;
	final private NodeModel child = testObjects.child;
	final private ChildrenUpdated childrenUpdated = testObjects.childrenUpdated;
	final private NodeModel parent2 = testObjects.parent2;
	final private NodeModel child2 = testObjects.child2;
	final private ChildrenUpdated childrenUpdated2 = testObjects.childrenUpdated2;

	@BeforeClass
	static public void setupClass() throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable() {
			
			@Override
			public void run() {
				// intentionally left blank
			}
		});
	}
	

	@Before
	public void setup() {
		createTestedInstance(1);

	}


	private void createTestedInstance(final int expectedEventCount) {
		consumer = new UpdatesEventCaptor(expectedEventCount);
		MapUpdateTimer timer = new MapUpdateTimer(consumer, DELAY_MILLIS, header);
		uut = new ChildrenUpdateGenerator(timer, eventFactory);
	}
	
	@Test
	public void generatesEventOnNodeInsertion() throws Exception {
		
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);
		uut.onNodeInserted(parent, child);
		
		final UpdatesFinished event = consumer.getEvent(TIMEOUT, TimeUnit.MILLISECONDS);
		UpdatesFinished expected = UpdatesFinished.builder()
				.mapId(header.mapId()).mapRevision(1)
				.addUpdateEvents(childrenUpdated).build();
		
		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}

	@Test
	public void generatesOneUpdateEventPerParent() throws Exception {
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);
		
		uut.onNodeInserted(parent, child);
		uut.onNodeInserted(parent, child);
		
		Thread.sleep(TIMEOUT);
		final UpdatesFinished event = consumer.getEvent(0, TimeUnit.MILLISECONDS);
		assertThat(event.updateEvents()).containsExactly(childrenUpdated);
	}
	
	@Test
	public void generatesEventOnNodeInsertionAfterDelay() throws Exception {
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);

		uut.onNodeInserted(parent, child);

		verifyZeroInteractions(eventFactory);
		consumer.getEvent(TIMEOUT, TimeUnit.MILLISECONDS);
	}

	@Test
	public void generatesMultipleEventsOnNodeInsertionToDifferentParents() throws Exception {
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);

		when(eventFactory.createChildrenUpdatedEvent(parent2)).thenReturn(childrenUpdated2);

		uut.onNodeInserted(parent, child);
		uut.onNodeInserted(parent2, child2);
		final UpdatesFinished event = consumer.getEvent(TIMEOUT, TimeUnit.MILLISECONDS);
		assertThat(event.updateEvents()).containsExactly(childrenUpdated, childrenUpdated2);
	}


	@Test
	public void generatesMultipleBatchesOnNodeInsertionToDifferentParentsWithPause() throws Exception {
		createTestedInstance(2);
		
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);
		when(eventFactory.createChildrenUpdatedEvent(parent2)).thenReturn(childrenUpdated2);

		uut.onNodeInserted(parent, child);
		Thread.sleep(TIMEOUT);
		uut.onNodeInserted(parent2, child2);
		final List<UpdatesFinished> events = consumer.getEvents(TIMEOUT, TimeUnit.MILLISECONDS);
		assertThat(events).hasSize(2);
		assertThat(events.get(0).updateEvents()).containsExactly(childrenUpdated);
		assertThat(events.get(1).updateEvents()).containsExactly(childrenUpdated2);
	}


	@Test
	public void generatesSpecialNodeTypeEventOnNodeInsertion() throws Exception {
		child.addExtension(SummaryNodeFlag.SUMMARY);
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);
		when(eventFactory.createChildrenUpdatedEvent(parent2)).thenReturn(childrenUpdated2);

		uut.onNodeInserted(parent, child);
		uut.onNodeInserted(parent2, child2);
		
		final UpdatesFinished event = consumer.getEvent(TIMEOUT, TimeUnit.MILLISECONDS);
		final SpecialNodeTypeSet specialNodeTypeUpdated = SpecialNodeTypeSet.builder()
				.nodeId(TestData.CHILD_NODE_ID).content(SpecialNodeType.SUMMARY_END).build();
		assertThat(event.updateEvents()).containsExactly(childrenUpdated, childrenUpdated2, specialNodeTypeUpdated);
	}

	@Test
	public void generatesEventsForInsertedChildOnNodeInsertion() throws Exception {
		child.insert(child2);
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);
		when(eventFactory.createChildrenUpdatedEvent(child)).thenReturn(childrenUpdated2);

		parent.insert(child);
		uut.onNodeInserted(parent, child);
		
		final UpdatesFinished event = consumer.getEvent(TIMEOUT, TimeUnit.MILLISECONDS);
		UpdatesFinished expected = UpdatesFinished.builder()
				.mapId(header.mapId()).mapRevision(1)
				.addUpdateEvents(childrenUpdated, childrenUpdated2).build();
		
		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}
	
	@Test
	public void generatesEventOnNewMap() throws Exception {
		when(map.getRootNode()).thenReturn(parent);
		uut.onNewMap(map);
		final UpdatesFinished event = consumer.getEvent(TIMEOUT, TimeUnit.MILLISECONDS);
		UpdatesFinished expected = UpdatesFinished.builder()
				.mapId(header.mapId()).mapRevision(1)
				.addUpdateEvents(RootNodeIdUpdated.builder().nodeId(TestData.PARENT_NODE_ID).build()).build();
		
		assertThat(event).isEqualTo(expected);
	}

}
