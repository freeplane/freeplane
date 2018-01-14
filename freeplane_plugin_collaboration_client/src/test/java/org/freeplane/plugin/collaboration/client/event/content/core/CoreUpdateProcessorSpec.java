package org.freeplane.plugin.collaboration.client.event.content.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.freeplane.collaboration.event.content.core.CoreMediaType;
import org.freeplane.collaboration.event.content.core.CoreUpdated;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.plugin.collaboration.client.event.TestObjects;
import org.freeplane.plugin.collaboration.client.event.content.core.CoreUpdateProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CoreUpdateProcessorSpec {
	@Mock
	private MTextController textController;
	@InjectMocks
	private CoreUpdateProcessor uut;
	
	final private TestObjects testObjects = new TestObjects();
	final private NodeModel node = testObjects.parent;
	final private MapModel map = testObjects.map;

	private CoreUpdated event(CoreMediaType mediaType, String content) {
		CoreUpdated coreUpdated= CoreUpdated.builder() //
				.nodeId(node.getID()).mediaType(mediaType).content(content).build();
		return coreUpdated;
	}

	
	@Test
	public void returnsEventClassCoreUpdated() throws Exception {
		assertThat(uut.eventClass()).isEqualTo(CoreUpdated.class);
	}
	
	@Test
	public void updatesPlainText() throws Exception {
		uut.onMapUpdated(map, event(CoreMediaType.PLAIN_TEXT, "content"));
		verify(textController).setNodeText(node, "content");
	}
	
	@Test
	public void updatesHtml() throws Exception {
		uut.onMapUpdated(map, event(CoreMediaType.HTML, "content"));
		verify(textController).setNodeText(node, "content");
	}
	
	@Test
	public void updatesNodeObject() throws Exception {
		uut.onMapUpdated(map, event(CoreMediaType.OBJECT, "java.lang.Integer|3"));
		verify(textController).setNodeObject(node, Integer.valueOf(3));
	}

}
