package org.freeplane.plugin.collaboration.client;

import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.SingleNodeStructureManipulator;
import org.freeplane.plugin.collaboration.client.UpdateSpecification.ContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChildrenUpdateShould {
	private static final String PARENT_NODE_ID = "id_1";

	@Mock
	private MapModel map;
	
	@Mock
	private SingleNodeStructureManipulator manipulator;

	@Test
	public void doNothing_ifOldAndNewChildrenListsAreEmpty() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID(PARENT_NODE_ID);
		
		when(map.getNodeForID(PARENT_NODE_ID)).thenReturn(parent);

		UpdateSpecification specification = ImmutableUpdateSpecification.builder()
				.contentType(ContentType.CHILDREN)
				.nodeId(PARENT_NODE_ID).
				content("").build();
		new ChildrenUpdate(manipulator, map, specification).apply();
		verifyNoMoreInteractions(manipulator);

}
}
