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

	public boolean containsExtension(final Class<? extends IExtension> clazz) {
		return extensions.containsKey(clazz);
	}

	public Iterator<IExtension> extensionIterator() {
		return getExtensions().values().iterator();
	}

	public IExtension getExtension(final Class<? extends IExtension> clazz) {
		return getExtensions().get(clazz);
	}

	public Map<Class<? extends IExtension>, IExtension> getExtensions() {
		return extensions;
	}

	// use putExtension (IExtension)
	@Deprecated
	public void putExtension(final Class<? extends IExtension> clazz, final IExtension extension) {
		getExtensions().put(clazz, extension);
	}

	public void putExtension(final IExtension extension) {
		getExtensions().put(extension.getClass(), extension);
	}

	// use removeExtension(IExtension extension)
	@Deprecated
	public IExtension removeExtension(final Class<? extends IExtension> clazz) {
		return getExtensions().remove(clazz);
	}

	public boolean removeExtension(final IExtension extension) {
		return getExtensions().remove(extension.getClass()) != null;
	}
}
