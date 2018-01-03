package org.freeplane.plugin.collaboration.client.event.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Supplier;

import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;
import org.freeplane.plugin.collaboration.client.event.UpdatesEventCaptor;
import org.freeplane.plugin.collaboration.client.event.children.AwtThreadStarter;
import org.freeplane.plugin.collaboration.client.event.children.StructureUpdateEventFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

public class UpdatesSpec {
	private static final int DELAY_MILLIS = 10;

	@Mock
	private MapModel map;
	
	@Mock
	private StructureUpdateEventFactory eventFactory;
	
	private ModifiableUpdateHeaderExtension header = ModifiableUpdateHeaderExtension.create().setMapId("mapId").setMapRevision(0);

	@BeforeClass
	static public void setupClass() throws InterruptedException, InvocationTargetException {
		AwtThreadStarter.await();
	}

	
	@Test
	public void generatesBlockContainingSingleEvent() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		
		Updates uut = new Updates(consumer, DELAY_MILLIS, header);
		
		uut.addUpdateEvent("id", () -> childrenUpdated);
		
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
				.mapId(header.mapId()).mapRevision(1)
				.addUpdateBlock(childrenUpdated).build();
		
		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}

	
	@Test
	public void generatesBlockContainingTwoEvents() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		
		Updates uut = new Updates(consumer, DELAY_MILLIS, header);
		
		uut.addUpdateEvent("id", () -> childrenUpdated);
		uut.addUpdateEvent("id", () -> childrenUpdated);
		
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
				.mapId(header.mapId()).mapRevision(1)
				.addUpdateBlock(childrenUpdated, childrenUpdated).build();
		
		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}
	
	@Test
	public void generatesBlockContainingSingleEventIfCalledTwiceForSameElementAndSameSupplier() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		
		Updates uut = new Updates(consumer, DELAY_MILLIS, header);
		
		Supplier<MapUpdated> eventSupplier = () -> childrenUpdated;
		uut.addUpdateEvent("id", eventSupplier);
		uut.addUpdateEvent("id", eventSupplier);
		
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
				.mapId(header.mapId()).mapRevision(1)
				.addUpdateBlock(childrenUpdated).build();
		
		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}

	
	
	@Test
	public void generatesBlockContainingTwoEventsForDifferentElementsAndSameSupplier() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		
		Updates uut = new Updates(consumer, DELAY_MILLIS, header);
		
		uut.addUpdateEvent("id", () -> childrenUpdated);
		uut.addUpdateEvent("id2", () -> childrenUpdated);
		
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
				.mapId(header.mapId()).mapRevision(1)
				.addUpdateBlock(childrenUpdated, childrenUpdated).build();
		
		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}

	@Test
	public void supportsAddingNewListenerDuringActionExecution() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		
		Updates uut = new Updates(consumer, DELAY_MILLIS, header);
		
		uut.addUpdateEvents("id", () -> uut.addUpdateEvents(
				"id", () -> uut.addUpdateEvent(childrenUpdated)
			));
		
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
				.mapId(header.mapId()).mapRevision(1)
				.addUpdateBlock(childrenUpdated).build();
		
		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}

	
	@Test
	public void generatesTwoBlocksContainingSingleEvent() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(2);
		
		Updates uut = new Updates(consumer, DELAY_MILLIS, header);
		
		Supplier<MapUpdated> eventSupplier = () -> childrenUpdated;
		uut.addUpdateEvent("id", eventSupplier);
		Thread.sleep(DELAY_MILLIS * 2);
		uut.addUpdateEvent("id", eventSupplier);
		
		final List<UpdateBlockCompleted> event = consumer.getEvents();
		UpdateBlockCompleted expected1 = UpdateBlockCompleted.builder()
				.mapId(header.mapId()).mapRevision(1)
				.addUpdateBlock(childrenUpdated).build();
		
		UpdateBlockCompleted expected2 = UpdateBlockCompleted.builder()
				.mapId(header.mapId()).mapRevision(2)
				.addUpdateBlock(childrenUpdated).build();
		
		assertThat(event).containsExactly(expected1, expected2);
		assertThat(header.mapRevision()).isEqualTo(2);
	}

}
