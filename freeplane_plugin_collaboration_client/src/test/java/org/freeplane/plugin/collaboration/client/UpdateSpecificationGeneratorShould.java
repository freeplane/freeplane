package org.freeplane.plugin.collaboration.client;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

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

		UpdateSpecification expected = ImmutableChildrenUpdateSpecification.builder()
				.nodeId(parent.getID())
				.content(Collections.<String>emptyList())
				.build();
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
		
		UpdateSpecification expected = ImmutableChildrenUpdateSpecification.builder()
				.nodeId(parent.getID())
				.content(asList("childId")).build();
		assertThat(result).isEqualTo(expected);
	}
	
	@Test
	public void createsUpdateForNodeWithTwoChildren() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID("nodeId");
		final NodeModel child = new NodeModel(map);
		child.setID("childId");
		parent.insert(child);
		
		final NodeModel child2 = new NodeModel(map);
		child2.setID("childId2");
		parent.insert(child2);
		
		UpdateSpecification result = uut.createChildrenUpdate(parent);
		
		UpdateSpecification expected = ImmutableChildrenUpdateSpecification.builder()
				.nodeId(parent.getID())
				.content(asList("childId","childId2")).build();
		assertThat(result).isEqualTo(expected);
	}
}
