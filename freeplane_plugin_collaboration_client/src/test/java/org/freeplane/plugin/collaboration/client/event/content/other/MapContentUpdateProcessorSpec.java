package org.freeplane.plugin.collaboration.client.event.content.other;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.freeplane.collaboration.event.content.other.MapContentUpdated;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.NodeContentManipulator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MapContentUpdateProcessorSpec {
	@Mock
	private MapModel map;
	@Mock
	private NodeContentManipulator nodeContentManipulator;
	@InjectMocks
	private MapContentUpdateProcessor uut;

	@Test
	public void returnsEventClassContentUpdated() throws Exception {
		assertThat(uut.eventClass()).isEqualTo(MapContentUpdated.class);
	}
	
	@Test
	public void callsMapContentManipulator() throws Exception {
		uut.onUpdate(map, MapContentUpdated.builder().content("content").build());
		
		verify(nodeContentManipulator).updateMapContent(map, "content", ContentUpdateGenerator.getMapContentExtensions());
	}
}
