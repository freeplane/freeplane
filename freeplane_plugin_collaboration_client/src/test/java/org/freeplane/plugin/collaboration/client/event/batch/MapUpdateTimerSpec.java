package org.freeplane.plugin.collaboration.client.event.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;
import org.freeplane.plugin.collaboration.client.event.children.UpdateEventFactory;
import org.freeplane.plugin.collaboration.client.event.children.UpdatesEventCaptor;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

public class MapUpdateTimerSpec {
	private static final int TIMEOUT = 500;

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
	

	
	@Test
	public void generatesEventOnNodeInsertion() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		
		MapUpdateTimer uut = new MapUpdateTimer(consumer, DELAY_MILLIS, header);
		
		uut.addActionListener(e -> uut.addUpdateEvents(childrenUpdated));
		uut.restart();
		
		final UpdatesFinished event = consumer.getEvent(TIMEOUT, TimeUnit.MILLISECONDS);
		UpdatesFinished expected = UpdatesFinished.builder()
				.mapId(header.mapId()).mapRevision(1)
				.addUpdateEvents(childrenUpdated).build();
		
		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}

	
	@Test
	public void supportsAddingNewListenerDuringActionExecution() throws Exception {
		final MapUpdated childrenUpdated = mock(MapUpdated.class);
		UpdatesEventCaptor consumer = new UpdatesEventCaptor(1);
		
		MapUpdateTimer uut = new MapUpdateTimer(consumer, DELAY_MILLIS, header);
		
		uut.addActionListener(
			e1 -> uut.addActionListener(
				e2 -> uut.addUpdateEvents(childrenUpdated)
			));
		uut.restart();
		
		final UpdatesFinished event = consumer.getEvent(TIMEOUT, TimeUnit.MILLISECONDS);
		UpdatesFinished expected = UpdatesFinished.builder()
				.mapId(header.mapId()).mapRevision(1)
				.addUpdateEvents(childrenUpdated).build();
		
		assertThat(event).isEqualTo(expected);
		assertThat(header.mapRevision()).isEqualTo(1);
	}

}
