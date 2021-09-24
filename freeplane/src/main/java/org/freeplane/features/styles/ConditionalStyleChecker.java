package org.freeplane.features.styles;

import java.util.stream.Stream;

public class ConditionalStyleChecker {
	private ConditionalStyleModel[] conditionalStyleModels;
	ConditionalStyleChecker(ConditionalStyleModel... conditionalStyleModels){
		this.conditionalStyleModels = conditionalStyleModels;
	}
    
    public boolean dependsOnConditionRecursively(ConditionPredicate predicate ) {
        return conditionalStyleModels()
        .anyMatch(m -> m.dependsOnConditionRecursively(predicate));
    }
    
    public boolean dependsOnCondition(ConditionPredicate predicate ) {
        return conditionalStyleModels()
        .anyMatch(m -> m.dependsOnCondition(predicate));
    }

    private Stream<ConditionalStyleModel> conditionalStyleModels() {
        return Stream.of(conditionalStyleModels)
        .filter(m -> m != null);
    }
}
