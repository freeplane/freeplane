package org.freeplane.features.filter.condition;

import java.util.Collection;

public interface ICombinedCondition extends ICondition{
	Collection<ASelectableCondition> split();
	
	@Override
    default boolean checksParent() {
        return split().stream().anyMatch(ICondition::checksParent);
    }

	@Override
	default boolean checksAncestors() {
        return split().stream().anyMatch(ICondition::checksAncestors);
    }

	@Override
	default boolean checksChildren() {
        return split().stream().anyMatch(ICondition::checksChildren);
    }

	@Override
	default boolean checksDescendants() {
        return split().stream().anyMatch(ICondition::checksDescendants);
    }

}
