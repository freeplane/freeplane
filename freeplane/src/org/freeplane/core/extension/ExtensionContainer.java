package org.freeplane.core.extension;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.freeplane.features.common.clipboard.ClipboardController;


/**
 * Contains an extension map and utility methods to handle them.
 * 
 * @author robert.ladstaetter
 */
public class ExtensionContainer {

	private final Map<Class<? extends IExtension>,IExtension> extensions = new HashMap<Class<? extends IExtension>, IExtension>();

	public Iterator<IExtension> extensionIterator() {
    	return getExtensions().values().iterator();
    }


	public IExtension getExtension(final Class<? extends IExtension> clazz) {
    	return getExtensions().get(clazz);
    }

	// use removeExtension(IExtension extension)
	@Deprecated
	public IExtension removeExtension(final Class<? extends IExtension> clazz) {
    	return getExtensions().remove(clazz);
    }

	public boolean removeExtension(final IExtension extension) {
    	return getExtensions().remove(extension.getClass()) != null;
    }

	// use putExtension (IExtension)
	@Deprecated
	public void putExtension(final Class<? extends IExtension> clazz, final IExtension extension) {
    	getExtensions().put(clazz, extension);
    }

	public void putExtension(IExtension extension) {
		getExtensions().put(extension.getClass(), extension);
	}
	
	public Map<Class<? extends IExtension>,IExtension> getExtensions() {
        return extensions;
    }
	
	public boolean containsExtension(Class<? extends IExtension> clazz) {
		return extensions.keySet().contains(clazz);
	}
	
	public ClipboardController getClipboardController() {
		return (ClipboardController)extensions.get(ClipboardController.class);
	}
	
}
