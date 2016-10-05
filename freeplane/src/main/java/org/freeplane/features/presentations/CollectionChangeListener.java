package org.freeplane.features.presentations;

public interface CollectionChangeListener<T extends NamedElement> {
	void onCollectionChange(CollectionChangedEvent<T> event);
}
