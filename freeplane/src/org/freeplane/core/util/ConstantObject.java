package org.freeplane.core.util;

public class ConstantObject<T, R extends Enum<?>> implements ObjectRule<T, R> {
	final private T object;

	public ConstantObject(T object) {
		this.object = object;
	}

	public T getValue() {
		return object;
	}

	public boolean hasValue() {
		return true;
	}

	public void resetCache() {
	}

	public R getRule() {
		return null;
	}

	public void setCache(T value) {
	}
}
