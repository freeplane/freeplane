package org.freeplane.plugin.collaboration.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.UpdateSpecification.ContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateSpecificationGeneratorShould {
	@Mock 
	private MapModel map;
	
	private UpdateSpecificationGenerator uut = new UpdateSpecificationGenerator();
	
	@Test
	public void createsUpdateForLeafNode() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID("nodeId");
		UpdateSpecification result = uut.createChildrenUpdate(parent);

		UpdateSpecification expected = ImmutableUpdate.builder().contentType(ContentType.CHILDREN).nodeId(parent.getID()).content("").build();
		assertThat(result).isEqualTo(expected);
	}
	
	@Test
	public void createsUpdateForNodeWithOneChild() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID("nodeId");
		final NodeModel child = new NodeModel(map);
		child.setID("childId");
		parent.insert(child);
		
		UpdateSpecification result = uut.createChildrenUpdate(parent);
		
		UpdateSpecification expected = ImmutableUpdate.builder().contentType(ContentType.CHILDREN).nodeId(parent.getID()).content("childId").build();
		assertThat(result).isEqualTo(expected);
	}
}
