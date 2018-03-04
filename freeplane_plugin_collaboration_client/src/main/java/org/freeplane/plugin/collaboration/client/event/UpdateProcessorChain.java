package org.freeplane.plugin.collaboration.client.event;

import java.util.HashMap;

import org.freeplane.collaboration.event.MapUpdated;
import org.freeplane.collaboration.event.batch.UpdateBlockCompleted;
import org.freeplane.features.map.MapModel;
import org.freeplane.plugin.collaboration.client.event.batch.Updates;

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


	public void onUpdate(MapModel map, final UpdateBlockCompleted updates) {
		for (MapUpdated event : updates.updateBlock())
			onUpdate(map, event);
	}
}
