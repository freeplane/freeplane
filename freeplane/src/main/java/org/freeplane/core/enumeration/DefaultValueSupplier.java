package org.freeplane.core.enumeration;

public interface DefaultValueSupplier <T extends DefaultValueSupplier<T>>{
	default T getDefaultValue() {
		return null;
	}
}
