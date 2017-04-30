package org.freeplane.features.styles;

import org.freeplane.features.filter.condition.ICondition;

public interface ConditionPredicate {
	boolean test(ICondition condition);
}
