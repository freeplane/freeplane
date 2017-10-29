package org.freeplane.plugin.collaboration.client.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdateProcessor;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdated;
import org.junit.Test;

public class UpdateProcessorsSpec {

	@Test
	public void callsRegisteredProcessorForUpdateEvent() throws Exception {
		final UpdateProcessors updateProcessors = new UpdateProcessors();
		final ChildrenUpdateProcessor processor = mock(ChildrenUpdateProcessor.class);
		when(processor.eventClass()).thenReturn(ChildrenUpdated.class);
		updateProcessors.addProcessor(processor);

		MapModel map = mock(MapModel.class);
		ChildrenUpdated event = mock(ChildrenUpdated.class);
		updateProcessors.onUpdate(map, event);
		verify(processor).onMapUpdated(map, event);
	}


	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionIfNoProcessorIsFound() throws Exception {
		final UpdateProcessors updateProcessors = new UpdateProcessors();
		final ChildrenUpdateProcessor processor = mock(ChildrenUpdateProcessor.class);
		when(processor.eventClass()).thenReturn(ChildrenUpdated.class);
		updateProcessors.addProcessor(processor);

		MapUpdated event = mock(MapUpdated.class);
		MapModel map = mock(MapModel.class);
		updateProcessors.onUpdate(map, event);
	}
}
