package org.freeplane.core.util;

public class ConstantObject<T, R extends Enum<?>> implements ObjectRule<T, R> {
	final private T object;

	public ConstantObject(T object) {
		this.object = object;
	}

	@Override
	public T getValue() {
		return object;
	}

	@Override
	public boolean hasValue() {
		return true;
	}

	@Override
	public void resetCache() {
	}

	@Override
	public R getRule() {
		return null;
	}

	@Override
	public void setCache(T value) {
	}
}
