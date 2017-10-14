package org.freeplane.plugin.collaboration.client.event.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdated;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeSet;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeSet.SpecialNodeType;
import org.freeplane.plugin.collaboration.client.event.children.UpdateEventFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MapUpdateTimerSpec {
	
	private static final int TIMEOUT = 100;

	private static final int DELAY_MILLIS = 10;

	@Mock
	private MapModel map;
	
	@Mock
	private UpdateEventFactory eventFactory;
	
	private ModifiableUpdateHeaderExtension header = ModifiableUpdateHeaderExtension.create().setMapId("mapId").setMapRevision(0);

	@BeforeClass
	static public void setupClass() throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable() {
			
			@Override
			public void run() {
				// intentionally left blank
			}
		});
	}
	

	private NodeModel createNode(String id) {
		NodeModel node = new NodeModel(map);
		node.setID(id);
		return node;
	}
	
	@Test
	public void generatesEventOnNodeInsertion() throws Exception {
		final NodeModel parent = createNode(TestData.PARENT_NODE_ID);
		final NodeModel child = createNode(TestData.CHILD_NODE_ID);
		final ChildrenUpdated childrenUpdated = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);

		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		
		MapUpdateTimer uut = new MapUpdateTimer(consumer, eventFactory, DELAY_MILLIS, header);
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
		final NodeModel parent = createNode(TestData.PARENT_NODE_ID);
		final NodeModel child = createNode(TestData.CHILD_NODE_ID);
		final ChildrenUpdated childrenUpdated = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);

		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		
		MapUpdateTimer uut = new MapUpdateTimer(consumer, eventFactory, DELAY_MILLIS, header);
		
		uut.onNodeInserted(parent, child);
		uut.onNodeInserted(parent, child);
		
		Thread.sleep(TIMEOUT);
		final UpdatesFinished event = consumer.getEvent(0, TimeUnit.MILLISECONDS);
		assertThat(event.updateEvents()).containsExactly(childrenUpdated);
	}
	
	@Test
	public void generatesEventOnNodeInsertionAfterDelay() throws Exception {
		final NodeModel parent = createNode(TestData.PARENT_NODE_ID);
		final NodeModel child = createNode(TestData.CHILD_NODE_ID);
		final ChildrenUpdated childrenUpdated = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		MapUpdateTimer uut = new MapUpdateTimer(consumer, eventFactory, DELAY_MILLIS, header);
		uut.onNodeInserted(parent, child);
		verifyZeroInteractions(eventFactory);
		consumer.getEvent(TIMEOUT, TimeUnit.MILLISECONDS);
	}

	@Test
	public void generatesMultipleEventsOnNodeInsertionToDifferentParents() throws Exception {
		final NodeModel parent = createNode(TestData.PARENT_NODE_ID);
		final NodeModel child = createNode(TestData.CHILD_NODE_ID);
		final NodeModel parent2 = createNode(TestData.PARENT_NODE_ID2);
		final NodeModel child2 = createNode(TestData.CHILD_NODE_ID2);
		final ChildrenUpdated childrenUpdated = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);

		final ChildrenUpdated childrenUpdated2 = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent2)).thenReturn(childrenUpdated2);

		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		MapUpdateTimer uut = new MapUpdateTimer(consumer, eventFactory, DELAY_MILLIS, header);
		uut.onNodeInserted(parent, child);
		uut.onNodeInserted(parent2, child2);
		final UpdatesFinished event = consumer.getEvent(TIMEOUT, TimeUnit.MILLISECONDS);
		assertThat(event.updateEvents()).containsExactly(childrenUpdated, childrenUpdated2);
	}


	@Test
	public void generatesMultipleBatchesOnNodeInsertionToDifferentParentsWithPause() throws Exception {
		final NodeModel parent = createNode(TestData.PARENT_NODE_ID);
		final NodeModel child = createNode(TestData.CHILD_NODE_ID);
		final NodeModel parent2 = createNode(TestData.PARENT_NODE_ID2);
		final NodeModel child2 = createNode(TestData.CHILD_NODE_ID2);
		final ChildrenUpdated childrenUpdated = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);

		final ChildrenUpdated childrenUpdated2 = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent2)).thenReturn(childrenUpdated2);

		UpdatesEventCaptor consumer = new UpdatesEventCaptor(2);
		MapUpdateTimer uut = new MapUpdateTimer(consumer, eventFactory, DELAY_MILLIS, header);
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
		final NodeModel parent = createNode(TestData.PARENT_NODE_ID);
		final NodeModel child = createNode(TestData.CHILD_NODE_ID);
		child.addExtension(SummaryNodeFlag.SUMMARY);
		final NodeModel parent2 = createNode(TestData.PARENT_NODE_ID2);
		final NodeModel child2 = createNode(TestData.CHILD_NODE_ID2);
		
		final ChildrenUpdated childrenUpdated = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent)).thenReturn(childrenUpdated);
		final ChildrenUpdated childrenUpdated2 = mock(ChildrenUpdated.class);
		when(eventFactory.createChildrenUpdatedEvent(parent2)).thenReturn(childrenUpdated2);

		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		
		MapUpdateTimer uut = new MapUpdateTimer(consumer, eventFactory, DELAY_MILLIS, header);
		uut.onNodeInserted(parent, child);
		uut.onNodeInserted(parent2, child2);
		
		final UpdatesFinished event = consumer.getEvent(TIMEOUT, TimeUnit.MILLISECONDS);
		final SpecialNodeTypeSet specialNodeTypeUpdated = SpecialNodeTypeSet.builder()
				.nodeId(TestData.CHILD_NODE_ID).content(SpecialNodeType.SUMMARY_END).build();
		assertThat(event.updateEvents()).containsExactly(childrenUpdated, childrenUpdated2, specialNodeTypeUpdated);
	}
}
