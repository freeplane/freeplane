package org.freeplane.plugin.collaboration.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class UpdateProcessorsSpec {

	@Test
	public void returnsRegisteredProcessorForUpdateEvent() throws Exception {
		final UpdateProcessors updateProcessors = new UpdateProcessors();
		updateProcessors.addProcessor(ChildrenUpdated.class, mock(ChildrenUpdateProcessor.class));

		MapUpdated event = mock(ChildrenUpdated.class);
		UpdateProcessor updateProcessor = updateProcessors.getProcessor(event);

		assertThat(updateProcessor).isInstanceOf(ChildrenUpdateProcessor.class);
	}


	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionIfNoProcessorIsFound() throws Exception {
		final UpdateProcessors updateProcessors = new UpdateProcessors();
		updateProcessors.addProcessor(ChildrenUpdated.class, mock(ChildrenUpdateProcessor.class));

		MapUpdated event = mock(MapUpdated.class);
		updateProcessors.getProcessor(event);
	}
}
