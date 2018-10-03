package org.freeplane.core.extension;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Configurable {
	void putClientProperty(Object key, Object value);
	Object getClientProperty(Object key);
	void refresh();
	default void addExtension(final Class<? extends IExtension> clazz, final IExtension extension) {
		final IExtension oldExtension = (IExtension) getClientProperty(extension.getClass());
		if (oldExtension == null)
			putClientProperty(clazz, extension);
		else if (!oldExtension.equals(extension)) {
			throw new RuntimeException("extension of class " + clazz.getName() + " already registered");
		}
	}

	default void addExtension(final IExtension extension) {
		addExtension(extension.getClass(), extension);
	}

	@SuppressWarnings("unchecked")
	default <T extends IExtension> T putExtension(final Class<? extends IExtension> clazz, final T extension) {
		final T oldExtension = (T) getClientProperty(clazz);
		putClientProperty(clazz, extension);
		return oldExtension;
	}

	@SuppressWarnings("unchecked")
	default <T extends IExtension> T putExtension(final T extension) {
		return putExtension(extension.getClass(), extension);
	}

	default boolean containsExtension(final Class<? extends IExtension> clazz) {
		return getClientProperty(clazz) != null;
	}

	@SuppressWarnings("unchecked")
	default <T extends IExtension> T getExtension(final Class<T> clazz) {
		return (T) getClientProperty(clazz);
	}


	@SuppressWarnings("unchecked")
	default <T extends IExtension> T computeIfAbsent(final Class<T> clazz, Function<Configurable, T> factory) {
		T extension = (T) getClientProperty(clazz);
		if(extension == null){
			extension = factory.apply(this);
			putClientProperty(clazz, extension);
		}
		return extension;
	}

	@SuppressWarnings("unchecked")
	default <T extends IExtension> T computeIfAbsent(final Class<T> clazz, Supplier<T> factory) {
		T extension = (T) getClientProperty(clazz);
		if(extension == null){
			extension = factory.get();
			putClientProperty(clazz, extension);
		}
		return extension;
	}

	@SuppressWarnings("unchecked")
	default <T extends IExtension> T removeExtension(final Class<T> clazz) {
		return (T) putExtension(clazz, null);
	}

	default boolean removeExtension(final IExtension extension) {
		return putExtension(extension.getClass(), null) != null;
	}

}
