package org.freeplane.plugin.collaboration.client.event.children_deprecated;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.children_deprecated.ChildrenUpdated.Side;
import org.junit.Before;
import org.junit.Test;

public class ChildrenUpdatedSpec{

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
	
	@Test
	public void sideOfIdStringIsEmpty() throws Exception {
		Optional<Side> maybeSide = Side.of("ID");
		assertThat(maybeSide.isPresent()).isFalse();
	}
	
	@Test
	public void sideOfLeftStringIsLeft() throws Exception {
		Optional<Side> maybeSide = Side.of("LEFT");
		assertThat(maybeSide.get()).isEqualTo(Side.LEFT);
	}
}
