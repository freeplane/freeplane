package org.freeplane.plugin.collaboration.client.event.children;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.freeplane.features.map.FirstGroupNodeFlag;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNodeFlag;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeSet.SpecialNodeType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SpecialNodeTypeSetSpec{
	@Mock 
	private MapModel map;

	@Test
	public void createsSpecialNodeTypeForSummaryEndNode() throws Exception {
		final NodeModel node = new NodeModel(map);
		node.addExtension(SummaryNodeFlag.SUMMARY);
		Optional<SpecialNodeType> result = SpecialNodeType.of(node);
		assertThat(result).contains(SpecialNodeType.SUMMARY_END);
	}


	@Test
	public void createsSpecialNodeTypeForSummaryBeginNode() throws Exception {
		final NodeModel node = new NodeModel(map);
		node.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
		Optional<SpecialNodeType> result = SpecialNodeType.of(node);
		assertThat(result).contains(SpecialNodeType.SUMMARY_BEGIN);
	}

	@Test
	public void createsSpecialNodeTypeForSummaryBeginEndNode() throws Exception {
		final NodeModel node = new NodeModel(map);
		node.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
		node.addExtension(SummaryNodeFlag.SUMMARY);
		Optional<SpecialNodeType> result = SpecialNodeType.of(node);
		assertThat(result).contains(SpecialNodeType.SUMMARY_BEGIN_END);
	}
}
