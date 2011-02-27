package org.freeplane.features.common.text;

public abstract class AbstractTextTransformer implements ITextTransformer {
	private int priority;

	public AbstractTextTransformer(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	public int compareTo(ITextTransformer that) {
		int thatPriority = that.getPriority();
		return (this.priority < thatPriority ? -1 : (this.priority == thatPriority ? 0 : 1));
	}
}
