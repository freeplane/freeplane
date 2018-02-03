package org.freeplane.plugin.collaboration.client.event.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Supplier;

import org.freeplane.collaboration.event.MapUpdated;
import org.freeplane.collaboration.event.batch.ModifiableUpdateHeader;
import org.freeplane.collaboration.event.batch.UpdateBlockCompleted;
import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.UpdatesEventCaptor;
import org.freeplane.plugin.collaboration.client.event.children.AwtThreadStarter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

public class UpdatesSpec {
	private static final int DELAY_MILLIS = 10;
	private static final String USER_ID="userID";
	@Mock
	private MapModel map;
	private ModifiableUpdateHeader header = ModifiableUpdateHeader.create().setMapId("mapId").setMapRevision(0);

	@BeforeClass
	static public void setupClass() throws InterruptedException, InvocationTargetException {
		AwtThreadStarter.await();
	}

	@Test
	public void generatesBlockContainingSingleEvent() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		Updates uut = new Updates(USER_ID, consumer, DELAY_MILLIS, header);
		uut.addUpdateEvent("id", () -> childrenUpdated);
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(1)
		    .addUpdateBlock(childrenUpdated).build();
		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}

	@Test
	public void generatesBlockContainingTwoEvents() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		Updates uut = new Updates(USER_ID, consumer, DELAY_MILLIS, header);
		uut.addUpdateEvent("id", () -> childrenUpdated);
		uut.addUpdateEvent("id", () -> childrenUpdated);
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(1)
		    .addUpdateBlock(childrenUpdated, childrenUpdated).build();
		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}

	@Test
	public void generatesBlockContainingSingleEventIfCalledTwiceForSameElementAndSameSupplier() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		Updates uut = new Updates(USER_ID, consumer, DELAY_MILLIS, header);
		Supplier<MapUpdated> eventSupplier = () -> childrenUpdated;
		uut.addUpdateEvent("id", eventSupplier);
		uut.addUpdateEvent("id", eventSupplier);
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(1)
		    .addUpdateBlock(childrenUpdated).build();
		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}

	@Test
	public void generatesBlockContainingTwoEventsForDifferentElementsAndSameSupplier() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		Updates uut = new Updates(USER_ID, consumer, DELAY_MILLIS, header);
		uut.addUpdateEvent("id", () -> childrenUpdated);
		uut.addUpdateEvent("id2", () -> childrenUpdated);
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(1)
		    .addUpdateBlock(childrenUpdated, childrenUpdated).build();
		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}

	@Test
	public void supportsAddingNewEventsDuringActionExecution() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		Updates uut = new Updates(USER_ID, consumer, DELAY_MILLIS, header);
		uut.addUpdateEvents("id", () -> uut.addUpdateEvents(
		    "id", () -> uut.addUpdateEvent(childrenUpdated)));
		final UpdateBlockCompleted event = consumer.getEvent();
		UpdateBlockCompleted expected = UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(1)
		    .addUpdateBlock(childrenUpdated).build();
		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}

	@Test
	public void generatesTwoBlocksContainingSingleEvent() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(2);
		Updates uut = new Updates(USER_ID, consumer, DELAY_MILLIS, header);
		Supplier<MapUpdated> eventSupplier = () -> childrenUpdated;
		uut.addUpdateEvent("id", eventSupplier);
		Thread.sleep(DELAY_MILLIS * 2);
		uut.addUpdateEvent("id", eventSupplier);
		final List<UpdateBlockCompleted> event = consumer.getEvents();
		UpdateBlockCompleted expected1 = UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(1)
		    .addUpdateBlock(childrenUpdated).build();
		UpdateBlockCompleted expected2 = UpdateBlockCompleted.builder()
			.userId(USER_ID)
		    .mapId(header.mapId()).mapRevision(2)
		    .addUpdateBlock(childrenUpdated).build();
		assertThat(event).containsExactly(expected1, expected2);
		assertThat(header.mapRevision()).isEqualTo(2);
	}
}
