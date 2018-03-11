package org.freeplane.plugin.collaboration.client.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.freeplane.collaboration.event.MapUpdated;
import org.freeplane.collaboration.event.NodeUpdated;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class UpdateProcessorChainSpec {
	@Mock
	private Controller controller;
	@InjectMocks
	private UpdateProcessorChain uut;

	@Test
	public void callsRegisteredProcessorForUpdateEvent() throws Exception {
		final UpdateProcessor processor = mock(UpdateProcessor.class);
		when(processor.eventClass()).thenReturn(MapUpdated.class);
		uut.add(processor);
		MapModel map = mock(MapModel.class);
		MapUpdated event = mock(MapUpdated.class);
		uut.onUpdate(map, event);
		verify(processor).onMapUpdated(map, event);
	}

	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionIfNoProcessorIsFound() throws Exception {
		final UpdateProcessor processor = mock(UpdateProcessor.class);
		when(processor.eventClass()).thenReturn(MapUpdated.class);
		uut.add(processor);
		MapUpdated event = mock(NodeUpdated.class);
		MapModel map = mock(MapModel.class);
		uut.onUpdate(map, event);
	}
}
