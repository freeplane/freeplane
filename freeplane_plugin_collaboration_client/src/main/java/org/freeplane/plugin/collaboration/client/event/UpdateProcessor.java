package org.freeplane.plugin.collaboration.client.event;

import org.freeplane.features.map.MapModel;

public interface UpdateProcessor<T extends MapUpdated> {
	 void onUpdate(MapModel map, T event);
	 
	 Class<T> eventClass();
	 
	 @SuppressWarnings("unchecked")
	 default void onMapUpdated(MapModel map, MapUpdated event){
		 onUpdate(map, (T)event);
	 }
}
