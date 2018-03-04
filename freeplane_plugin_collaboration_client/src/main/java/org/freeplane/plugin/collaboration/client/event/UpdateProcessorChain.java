package org.freeplane.plugin.collaboration.client.event;

import java.util.HashMap;
import java.util.List;

import org.freeplane.collaboration.event.MapUpdated;
import org.freeplane.features.map.MapModel;

public class UpdateProcessorChain{
	
	private final HashMap<Class<? extends MapUpdated>, UpdateProcessor<? extends MapUpdated> > processors = new HashMap<>();

	public <T extends MapUpdated> UpdateProcessorChain add(UpdateProcessor<T> processor) {
		processors.put(processor.eventClass(), processor);
		return this;
	}
	

	public void onUpdate(MapModel map, MapUpdated event) {
		final Class<?>[] interfaces = event.getClass().getInterfaces();
		for(Class<?> eventClassCandidate : interfaces) {
			final UpdateProcessor<?> updateProcessor = processors.get(eventClassCandidate);
			if(updateProcessor != null) {
				updateProcessor.onMapUpdated(map, event);
				return;
			}
			
		}
		throw new IllegalArgumentException("No processor available for " + event);
	}


	public void onUpdate(MapModel map, List<MapUpdated> events ) {
		for (MapUpdated event : events)
			onUpdate(map, event);
	}
}
