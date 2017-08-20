package org.freeplane.plugin.collaboration.client.event.children;

import java.util.Optional;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.plugin.collaboration.client.event.NodeUpdated;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableSpecialNodeTypeSet.class)
@JsonDeserialize(as = ImmutableSpecialNodeTypeSet.class)
public interface SpecialNodeTypeSet extends NodeUpdated{
	
	enum SpecialNodeType {
		SUMMARY_BEGIN, SUMMARY_END, SUMMARY_BEGIN_END;
		
		public static Optional<SpecialNodeType> of(NodeModel node) {
			final Optional<SpecialNodeType> content;
			final boolean isFirstGroupNode = SummaryNode.isFirstGroupNode(node);
			final boolean isSummaryNode = SummaryNode.isSummaryNode(node);
			if(isSummaryNode && isFirstGroupNode)
				content = Optional.of(SpecialNodeType.SUMMARY_BEGIN_END);
			else if(isFirstGroupNode)
				content = Optional.of(SpecialNodeType.SUMMARY_BEGIN);
			else if(isSummaryNode)
				content = Optional.of(SpecialNodeType.SUMMARY_END);
			else
				content = Optional.empty();
			return content;
		}

	}
	
	static ImmutableSpecialNodeTypeSet.Builder builder() {
		return ImmutableSpecialNodeTypeSet.builder();
	}
	
	SpecialNodeType content();
}
