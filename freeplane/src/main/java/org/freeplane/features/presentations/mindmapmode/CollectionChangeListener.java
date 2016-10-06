package org.freeplane.features.presentations.mindmapmode;

public interface CollectionChangeListener<T extends NamedElement<T>> {
	void onCollectionChange(CollectionChangedEvent<T> event);
}
