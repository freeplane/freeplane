package org.freeplane.plugin.collaboration.client.event.children_deprecated;

import java.util.List;
import java.util.Optional;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.event.NodeUpdated;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableChildrenUpdated.class)
@JsonDeserialize(as = ImmutableChildrenUpdated.class)
public interface ChildrenUpdated extends NodeUpdated{
	
	@Value.Immutable
	@JsonSerialize(as = ImmutableChild.class)
	@JsonDeserialize(as = ImmutableChild.class)
	@JsonInclude(value=Include.NON_ABSENT)
	interface Child {
		static public ImmutableChild.Builder builder() { return ImmutableChild.builder();}
		String id();
		Optional<Side> side();
	}

	
	public enum Side{
		LEFT, RIGHT;

		public static Side of(NodeModel child) {
			return child.isLeft() ? LEFT : RIGHT;
		}

		public static Optional<Side> of(String string) {
			try {
				return Optional.of(Side.valueOf(string));
			} catch (IllegalArgumentException e) {
				return Optional.empty();
			}
		}
		
		public boolean isLeft() {
			return this == Side.LEFT;
		}
	}

	static ImmutableChildrenUpdated.Builder builder() {
		return ImmutableChildrenUpdated.builder();
	}
	
	List<Child> content();
}
