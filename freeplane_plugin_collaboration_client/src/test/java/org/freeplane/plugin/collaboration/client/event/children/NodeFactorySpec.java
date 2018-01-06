package org.freeplane.plugin.collaboration.client.event.children;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.junit.Test;

public class NodeFactorySpec {
@Test
public void createsNodeWithGivenMapAndId() throws Exception {
	NodeFactory uut = new NodeFactory();
	MapModel map = mock(MapModel.class);
	final NodeModel node = uut.createNode(map);
	assertThat(node).isNotNull();
}
}
