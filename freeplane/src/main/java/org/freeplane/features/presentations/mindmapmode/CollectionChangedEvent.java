package org.freeplane.features.presentations.mindmapmode;

public class CollectionChangedEvent<T extends NamedElement<T>> {
	public enum EventType {SELECTION_CHANGED, SELECTION_INDEX_CHANGED, COLLECTION_SIZE_CHANGED;

		public <T extends NamedElement<T>> CollectionChangedEvent<T> of(NamedElementCollection<T> collection) {
			return new CollectionChangedEvent<>(this, collection);
		}
	}

	public final EventType eventType;
	public final NamedElementCollection<T> collection;
	public CollectionChangedEvent(EventType eventType, NamedElementCollection<T> collection) {
		super();
		this.eventType = eventType;
		this.collection = collection;
	}
	
}
