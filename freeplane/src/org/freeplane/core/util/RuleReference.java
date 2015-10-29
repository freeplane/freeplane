package org.freeplane.core.util;


public class RuleReference<T, R extends Enum<?>> implements ObjectRule<T, R> {
	final private R rule;
	private T value;

	public RuleReference(R rule) {
		this.rule = rule;
	}

	public T getValue() {
		return value;
	}

	public boolean hasValue() {
		return value != null;
	}

	public void resetCache() {
		value = null;
	}

	public R getRule() {
		return rule;
	}

	public void setCache(T value) {
		this.value = value;
		
	}
}
