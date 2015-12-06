package org.freeplane.core.ui.menubuilders.generic;

@SuppressWarnings("serial")
public class AttributeAlreadySetException extends RuntimeException {

	public AttributeAlreadySetException(Object key, Object object) {
		super("Attribute " + key + " already has value " + String.valueOf(object));
	}

}
