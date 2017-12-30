package org.freeplane.core.extension;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
        final IExtension oldExtension = getExtensions().put(clazz, extension);
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

    @SuppressWarnings({"unchecked", "rawtypes"})
	public Map<Class<? extends IExtension>, ? extends IExtension> removeAll(final Collection<Class<? extends IExtension>> classes) {
    	Map removedExtensions = new HashMap();
    	for(Class c : classes) {
    		final IExtension removed = extensions.remove(c);
    		if(removed != null)
    			removedExtensions.put(c, removed);
    	}
		return removedExtensions;
	}
    
    @SuppressWarnings({"unchecked", "rawtypes"})
	public Map<Class<? extends IExtension>, ? extends IExtension> retainAll(Collection<Class<? extends IExtension>> classes) {
    	Map removedExtensions = new HashMap();
    	Set<Class<? extends IExtension>> knownExtensionClasses = extensions.keySet();
    	for(Class<?> c : knownExtensionClasses) {
    		if(! classes.contains(c)) {
    			final IExtension removed = extensions.remove(c);
    			removedExtensions.put(c, removed);
    		}
    	}
		return removedExtensions;
	}

    
	public void addAll(Map<Class<? extends IExtension>, ? extends IExtension> extensions) {
    	for(Entry<Class<? extends IExtension>, ? extends IExtension> еntry : extensions.entrySet()) {
    		addExtension(еntry.getKey(), еntry.getValue());
    	}
	}
    
	public boolean removeExtension(final IExtension extension) {
		return getExtensions().remove(extension.getClass()) != null;
	}

	public static void swapExtensions(ExtensionContainer firstContent, ExtensionContainer secondContent,
			Class<? extends IExtension> clazz) {
		final IExtension firstExtension = firstContent.getExtension(clazz);
		final IExtension secondExtension = secondContent.getExtension(clazz);
		if(secondExtension != null)
			firstContent.putExtension(secondExtension);
		else if(firstExtension != null)
			firstContent.removeExtension(clazz);

		if(firstExtension != null)
			secondContent.putExtension(firstExtension);
		else if(secondExtension != null)
			secondContent.removeExtension(clazz);
}

}
