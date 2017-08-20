package org.freeplane.plugin.collaboration.client.event.children;

import org.freeplane.plugin.collaboration.client.event.NodeUpdated;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableChildrenUpdated.class)
@JsonDeserialize(as = ImmutableChildrenUpdated.class)
public interface SpecialNodeTypeUpdated extends NodeUpdated{
	
	enum SpecialNodeType {
		SUMMARY_BEGIN, SUMMARY_END, SUMMARY_BEGIN_END
	}
	
	static ImmutableChildrenUpdated.Builder builder() {
		return ImmutableChildrenUpdated.builder();
	}
	
	SpecialNodeType content();
}
