package org.freeplane.plugin.collaboration.client.event.children;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.freeplane.collaboration.event.children.RootNodeIdUpdated;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.TestObjects;
import org.junit.Test;

public class RootNodeIdUpdatedProcessorSpec {
	
	private RootNodeIdUpdatedProcessor uut = new RootNodeIdUpdatedProcessor();
	private TestObjects testObjects = new TestObjects();
	private MapModel map = testObjects.map;
	private NodeModel parent = testObjects.parent;
	
	@Test
	public void returnsEventClass_RootNodeIdUpdated() throws Exception {
		assertThat(uut.eventClass()).isEqualTo(RootNodeIdUpdated.class);
	}
	
	
	@Test
	public void updatesRootNodeId() throws Exception {
		when(map.getRootNode()).thenReturn(parent);
		final String nodeId = "root node id";
		uut.onMapUpdated(map, RootNodeIdUpdated.builder().nodeId(nodeId).build());
		assertThat(parent.getID()).isEqualTo(nodeId);
	}
	

}
