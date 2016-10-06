package org.freeplane.features.presentations.mindmapmode;

public class CollectionChangedEvent<T extends NamedElement<T>> {
	public enum EventType {SELECTION_CHANGED, SELECTION_INDEX_CHANGED, COLLECTION_SIZE_CHANGED;

		public <T extends NamedElement<T>> CollectionChangedEvent<T> of(CollectionModel<T> collectionModel) {
			return new CollectionChangedEvent<>(this, collectionModel);
		}
	}

	public final EventType eventType;
	public final CollectionModel<T> collection;
	public CollectionChangedEvent(EventType eventType, CollectionModel<T> collection) {
		super();
		this.eventType = eventType;
		this.collection = collection;
	}
	
}
