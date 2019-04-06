package org.freeplane.core.extension;

import java.util.Iterator;
import java.util.Map;

/**
 * Contains an extension map and utility methods to handle them.
 *
 * @author robert.ladstaetter
 */
public class ExtensionContainer {
	private final Map<Class<? extends IExtension>, IExtension> extensions;

	public ExtensionContainer(final Map<Class<? extends IExtension>, IExtension> extensions) {
		super();
		this.extensions = extensions;
	}

	public void addExtension(final Class<? extends IExtension> clazz, final IExtension extension) {
		assert(clazz.isAssignableFrom(extension.getClass()));
		final IExtension oldExtension = getExtensions().put(clazz, extension);
		if (oldExtension != null && !oldExtension.equals(extension)) {
			getExtensions().put(clazz, oldExtension);
			throw new RuntimeException("extension of class " + clazz.getName() + " already registered");
		}
	}

	public void addExtension(final IExtension extension) {
		addExtension(extension.getClass(), extension);
	}

	public IExtension putExtension(final Class<? extends IExtension> clazz, final IExtension extension) {
        final IExtension oldExtension = extension != null ? extensions.put(clazz, extension) : extensions.remove(clazz);
		return oldExtension;
	}

	public IExtension putExtension(final IExtension extension) {
		return putExtension(extension.getClass(), extension);
	}

	public boolean containsExtension(final Class<? extends IExtension> clazz) {
		return extensions.containsKey(clazz);
	}

	public Iterator<IExtension> extensionIterator() {
		return getExtensions().values().iterator();
	}

	@SuppressWarnings("unchecked")
    public <T extends IExtension> T getExtension(final Class<T> clazz) {
		return (T) getExtensions().get(clazz);
	}

	public Map<Class<? extends IExtension>, IExtension> getExtensions() {
		return extensions;
	}

	@SuppressWarnings("unchecked")
    public <T extends IExtension> T removeExtension(final Class<T> clazz) {
		return (T) getExtensions().remove(clazz);
	}

	public boolean removeExtension(final IExtension extension) {
		return getExtensions().remove(extension.getClass()) != null;
	}
}
