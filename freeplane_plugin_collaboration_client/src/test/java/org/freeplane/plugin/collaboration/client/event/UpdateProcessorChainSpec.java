package org.freeplane.plugin.collaboration.client.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.freeplane.features.map.MapModel;
import org.junit.Test;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class UpdateProcessorChainSpec {

	@Test
	public void callsRegisteredProcessorForUpdateEvent() throws Exception {
		final UpdateProcessorChain updateProcessors = new UpdateProcessorChain();
		final UpdateProcessor processor = mock(UpdateProcessor.class);
		when(processor.eventClass()).thenReturn(MapUpdated.class);
		updateProcessors.add(processor);

		MapModel map = mock(MapModel.class);
		MapUpdated event = mock(MapUpdated.class);
		updateProcessors.onUpdate(map, event);
		verify(processor).onMapUpdated(map, event);
	}


	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionIfNoProcessorIsFound() throws Exception {
		final UpdateProcessorChain updateProcessors = new UpdateProcessorChain();
		final UpdateProcessor processor = mock(UpdateProcessor.class);
		when(processor.eventClass()).thenReturn(MapUpdated.class);
		updateProcessors.add(processor);

		MapUpdated event = mock(NodeUpdated.class);
		MapModel map = mock(MapModel.class);
		updateProcessors.onUpdate(map, event);
	}
}
