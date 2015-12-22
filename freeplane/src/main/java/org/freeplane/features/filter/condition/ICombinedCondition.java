package org.freeplane.features.filter.condition;

import java.util.Collection;

public interface ICombinedCondition {
	Collection<ASelectableCondition> split();
}
