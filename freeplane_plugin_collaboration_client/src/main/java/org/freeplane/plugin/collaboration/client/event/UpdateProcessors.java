package org.freeplane.plugin.collaboration.client.event;

import java.util.HashMap;

public class UpdateProcessors implements UpdateProcessor {
	
	private final HashMap<Class<?>, UpdateProcessor > processors = new HashMap<>();

	public UpdateProcessors addProcessor(Class<? extends MapUpdated> eventClass, UpdateProcessor processor) {
		processors.put(eventClass, processor);
		return this;
	}
	

	@Override
	public void onMapUpdated(MapUpdated event) {
		final Class<?>[] interfaces = event.getClass().getInterfaces();
		for(Class<?> eventClassCandidate : interfaces) {
			final UpdateProcessor updateProcessor = processors.get(eventClassCandidate);
			if(updateProcessor != null) {
				updateProcessor.onMapUpdated(event);
				return;
			}
			
		}
		throw new IllegalArgumentException("No processor available for " + event);
	}
}
