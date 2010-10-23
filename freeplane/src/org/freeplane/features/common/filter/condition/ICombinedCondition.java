package org.freeplane.features.common.filter.condition;

import java.util.Collection;

public interface ICombinedCondition {
	Collection<ASelectableCondition> split();
}
