package org.freeplane.plugin.collaboration.client.event.batch;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Supplier;

import org.freeplane.collaboration.event.MapUpdated;
import org.freeplane.collaboration.event.batch.ImmutableMapId;
import org.freeplane.collaboration.event.batch.MapId;
import org.freeplane.collaboration.event.batch.ModifiableUpdateHeader;
import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.UpdatesEventCaptor;
import org.freeplane.plugin.collaboration.client.event.children.AwtThreadStarter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

public class UpdatesSpec {
	private static final int DELAY_MILLIS = 10;
	private static final MapId MAP_ID = ImmutableMapId.of("mapId");
	@Mock
	private MapModel map;
	private ModifiableUpdateHeader header = ModifiableUpdateHeader.create().setMapId(MAP_ID).setMapRevision(0);

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
		final List<MapUpdated> eventList = consumer.getEventList();
		assertThat(eventList).containsExactly(childrenUpdated);
	}

	@Test
	public void generatesBlockContainingTwoEvents() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		Updates uut = new Updates(consumer, DELAY_MILLIS, header);
		uut.addUpdateEvent("id", () -> childrenUpdated);
		uut.addUpdateEvent("id", () -> childrenUpdated);
		final List<MapUpdated> eventList = consumer.getEventList();
		assertThat(eventList).containsExactly(childrenUpdated, childrenUpdated);
	}

	@Test
	public void generatesBlockContainingSingleEventIfCalledTwiceForSameElementAndSameSupplier() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		Updates uut = new Updates(consumer, DELAY_MILLIS, header);
		Supplier<MapUpdated> eventSupplier = () -> childrenUpdated;
		uut.addUpdateEvent("id", eventSupplier);
		uut.addUpdateEvent("id", eventSupplier);
		final List<MapUpdated> eventList = consumer.getEventList();
		assertThat(eventList).containsExactly(childrenUpdated);
	}

	@Test
	public void generatesBlockContainingTwoEventsForDifferentElementsAndSameSupplier() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		Updates uut = new Updates(consumer, DELAY_MILLIS, header);
		uut.addUpdateEvent("id", () -> childrenUpdated);
		uut.addUpdateEvent("id2", () -> childrenUpdated);
		final List<MapUpdated> eventList = consumer.getEventList();
		assertThat(eventList).containsExactly(childrenUpdated, childrenUpdated);
	}

	@Test
	public void supportsAddingNewEventsDuringActionExecution() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		Updates uut = new Updates(consumer, DELAY_MILLIS, header);
		uut.addUpdateEvents("id", () -> uut.addUpdateEvents(
		    "id", () -> uut.addUpdateEvent(childrenUpdated)));
		final List<MapUpdated> eventList = consumer.getEventList();
		assertThat(eventList).containsExactly(childrenUpdated);
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
		final List<List<MapUpdated>> eventLists = consumer.getEvents();
		assertThat(eventLists).containsExactly(asList(childrenUpdated), asList(childrenUpdated));
	}
}
