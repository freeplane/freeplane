package org.freeplane.features.presentations;

public interface NamedElement<T extends NamedElement<T>> {
	String getName();
	void setName(String name);
	T saveAs(String name);
}
