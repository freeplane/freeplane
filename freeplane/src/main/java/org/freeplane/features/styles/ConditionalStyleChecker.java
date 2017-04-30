package org.freeplane.features.styles;

public class ConditionalStyleChecker {
	private ConditionalStyleModel[] conditionalStyleModels;
	ConditionalStyleChecker(ConditionalStyleModel... conditionalStyleModels){
		this.conditionalStyleModels = conditionalStyleModels;
	}
	public boolean dependOnCondition(ConditionPredicate predicate ) {
		for(ConditionalStyleModel m : conditionalStyleModels){
			if(m != null){
				if(m.dependOnCondition(predicate))
					return true;
			}
		}
		return false;
	}

}
