package org.freeplane.plugin.collaboration.client.event.content.other;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.NodeContentManipulator;
import org.freeplane.plugin.collaboration.client.event.content.other.ContentUpdateGenerator;
import org.freeplane.plugin.collaboration.client.event.content.other.NodeContentUpdateProcessor;
import org.freeplane.plugin.collaboration.client.event.content.other.NodeContentUpdated;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NodeContentUpdateProcessorSpec {
	@Mock
	private MapModel map;
	@Mock
	private NodeContentManipulator nodeContentManipulator;
	@InjectMocks
	private NodeContentUpdateProcessor uut;

	@Test
	public void returnsEventClassContentUpdated() throws Exception {
		assertThat(uut.eventClass()).isEqualTo(NodeContentUpdated.class);
	}
	
	@Test
	public void callsNodeContentManipulator() throws Exception {
		final NodeModel node = new NodeModel(null);
		when(map.getNodeForID("nodeId")).thenReturn(node);
		uut.onUpdate(map, NodeContentUpdated.builder().nodeId("nodeId").content("content").build());
		
		verify(nodeContentManipulator).updateNodeContent(node, "content", ContentUpdateGenerator.getNodeContentExclusions());
	}
}
