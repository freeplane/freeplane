package org.freeplane.plugin.collaboration.client.event;

public interface UpdateProcessor<T extends MapUpdated> {
	 void onUpdate(T event);
	 
	 @SuppressWarnings("unchecked")
	 default void onMapUpdated(MapUpdated event){
		 onUpdate((T)event);
	 }
}
