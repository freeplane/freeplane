package org.freeplane.features.presentations;

public interface CollectionChangeListener<T extends NamedElement<T>> {
	void onCollectionChange(CollectionChangedEvent<T> event);
}
