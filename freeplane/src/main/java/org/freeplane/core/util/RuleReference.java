package org.freeplane.core.util;


public class RuleReference<T, R extends Enum<?>> implements ObjectRule<T, R> {
	final private R rule;
	private T value;

	public RuleReference(R rule) {
		this.rule = rule;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public boolean hasValue() {
		return value != null;
	}

	@Override
	public void resetCache() {
		value = null;
	}

	@Override
	public R getRule() {
		return rule;
	}

	@Override
	public void setCache(T value) {
		this.value = value;
		
	}
}
