package org.freeplane.plugin.collaboration.client.event.children;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.junit.Before;
import org.junit.Test;

public class SideSpec{

	private NodeModel node;
	
	@Before
	public void setup() {
		MapModel map = mock(MapModel.class);
		node = new NodeModel(map);
	}

	@Test
	public void sideOfRightNode() throws Exception {
		assertThat(Side.of(node)).isEqualTo(Side.RIGHT);
	}
	@Test
	public void sideOfLeftNode() throws Exception {
		node.setLeft(true);
		assertThat(Side.of(node)).isEqualTo(Side.LEFT);
	}
}
