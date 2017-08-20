package org.freeplane.plugin.collaboration.client.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdateProcessor;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdated;
import org.junit.Test;

public class UpdateProcessorsSpec {

	@Test
	public void callsRegisteredProcessorForUpdateEvent() throws Exception {
		final UpdateProcessors updateProcessors = new UpdateProcessors();
		final ChildrenUpdateProcessor processor = mock(ChildrenUpdateProcessor.class);
		updateProcessors.addProcessor(ChildrenUpdated.class, processor);

		MapUpdated event = mock(ChildrenUpdated.class);
		updateProcessors.onMapUpdated(event);
		verify(processor).onMapUpdated(event);
	}


	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionIfNoProcessorIsFound() throws Exception {
		final UpdateProcessors updateProcessors = new UpdateProcessors();
		updateProcessors.addProcessor(ChildrenUpdated.class, mock(ChildrenUpdateProcessor.class));

		MapUpdated event = mock(MapUpdated.class);
		updateProcessors.onMapUpdated(event);
	}
}
