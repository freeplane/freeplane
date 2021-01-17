package org.freeplane.features.text;

public abstract class AbstractContentTransformer implements IContentTransformer {
	private int priority;

	public AbstractContentTransformer(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	public int compareTo(IContentTransformer that) {
		int thatPriority = that.getPriority();
		return (this.priority < thatPriority ? -1 : (this.priority == thatPriority ? 0 : 1));
	}

	public boolean markTransformation() {
	    return false;
    }

	@Override
	public boolean isFormula(Object content) {
		return false;
	}
	
}
