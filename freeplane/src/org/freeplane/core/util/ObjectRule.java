package org.freeplane.core.util;

public interface ObjectRule<T, R extends Enum<?>> {
	T getValue();
	boolean hasValue();
	void resetCache();
	R getRule();
	void setCache(T value);
}
