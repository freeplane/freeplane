package org.freeplane.features.edge;

import org.freeplane.core.extension.IExtension;

public class AutomaticEdgeColor implements IExtension{
	public enum Rule {
		FOR_BRANCHES(true), FOR_LEVELS(true), FOR_COLUMNS(true), ON_BRANCH_CREATION(false);
		
		public final boolean isActiveOnCreation;
		public final boolean isDynamic;

		private Rule(boolean isDynamic) {
			this.isActiveOnCreation = ! isDynamic;
			this.isDynamic = isDynamic;
		} 
		};
	private int colorCount; 
	final public Rule rule;
	public int getColorCounter() {
    	return colorCount;
    }
	public AutomaticEdgeColor(Rule rule, int colorCount) {
	    super();
		this.rule = rule;
	    this.colorCount = colorCount;
    }

	void increaseColorCounter() {
		colorCount++;
    }
}