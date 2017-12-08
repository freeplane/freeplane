package org.freeplane.plugin.collaboration.client.event.content;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ContentUpdateProcessorSpec {
	private ContentUpdateProcessor uut = new ContentUpdateProcessor();

	@Test
	public void returnsEventClassContentUpdated() throws Exception {
		assertThat(uut.eventClass()).isEqualTo(ContentUpdated.class);
	}
}
