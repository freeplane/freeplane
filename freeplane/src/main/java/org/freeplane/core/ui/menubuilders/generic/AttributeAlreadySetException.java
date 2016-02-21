package org.freeplane.core.ui.menubuilders.generic;

@SuppressWarnings("serial")
public class AttributeAlreadySetException extends RuntimeException {

	public AttributeAlreadySetException(Entry entry, Object key, Object object) {
		super("In entry " + entry.getPath() + " attribute " + key + " already has value " + String.valueOf(object));
	}

}
