package org.freeplane.features.presentations.mindmapmode;

public interface NamedElementFactory<T extends NamedElement<T>> {
	T create(String name);
	T create(T prototype, String newName);
}
