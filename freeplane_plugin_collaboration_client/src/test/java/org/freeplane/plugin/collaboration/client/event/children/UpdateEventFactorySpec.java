package org.freeplane.plugin.collaboration.client.event.children;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.freeplane.features.map.FirstGroupNodeFlag;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNodeFlag;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeUpdated.SpecialNodeType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateEventFactorySpec {
	@Mock 
	private MapModel map;
	
	private UpdateEventFactory uut = new UpdateEventFactory();
	
	@Test
	public void createsUpdateForLeafNode() throws Exception {
		final NodeModel parent = new NodeModel(map);
		parent.setID("nodeId");
		MapUpdated result = uut.createChildrenUpdatedEvent(parent);

		MapUpdated expected = ImmutableChildrenUpdated.builder()
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
		
		MapUpdated result = uut.createChildrenUpdatedEvent(parent);
		
		MapUpdated expected = ImmutableChildrenUpdated.builder()
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
		
		MapUpdated result = uut.createChildrenUpdatedEvent(parent);
		
		MapUpdated expected = ImmutableChildrenUpdated.builder()
				.nodeId(parent.getID())
				.content(asList("childId","childId2")).build();
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void createsUpdateForSummaryEndNode() throws Exception {
		final NodeModel node = new NodeModel(map);
		node.setID("nodeId");
		node.addExtension(SummaryNodeFlag.SUMMARY);
		MapUpdated result = uut.createSpecialNodeTypeUpdatedEvent(node);

		MapUpdated expected = ImmutableSpecialNodeTypeUpdated.builder()
				.nodeId(node.getID())
				.content(SpecialNodeType.SUMMARY_END)
				.build();
		assertThat(result).isEqualTo(expected);
	}


	@Test
	public void createsUpdateForSummaryBeginNode() throws Exception {
		final NodeModel node = new NodeModel(map);
		node.setID("nodeId");
		node.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
		MapUpdated result = uut.createSpecialNodeTypeUpdatedEvent(node);

		MapUpdated expected = ImmutableSpecialNodeTypeUpdated.builder()
				.nodeId(node.getID())
				.content(SpecialNodeType.SUMMARY_BEGIN)
				.build();
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void createsUpdateForSummaryBeginEndNode() throws Exception {
		final NodeModel node = new NodeModel(map);
		node.setID("nodeId");
		node.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
		node.addExtension(SummaryNodeFlag.SUMMARY);
		MapUpdated result = uut.createSpecialNodeTypeUpdatedEvent(node);

		MapUpdated expected = ImmutableSpecialNodeTypeUpdated.builder()
				.nodeId(node.getID())
				.content(SpecialNodeType.SUMMARY_BEGIN_END)
				.build();
		assertThat(result).isEqualTo(expected);
	}
}
