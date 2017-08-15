package org.freeplane.plugin.collaboration.client;

import java.util.HashMap;

public class UpdateProcessors {
	
	private final HashMap<Class<?>, UpdateProcessor > processors = new HashMap<>();

	public UpdateProcessors addProcessor(Class<? extends MapUpdated> eventClass, UpdateProcessor processor) {
		processors.put(eventClass, processor);
		return this;
	}
	
	public UpdateProcessor getProcessor(MapUpdated event) {
		final Class<?>[] interfaces = event.getClass().getInterfaces();
		for(Class<?> eventClassCandidate : interfaces) {
			final UpdateProcessor updateProcessor = processors.get(eventClassCandidate);
			if(updateProcessor != null)
				return updateProcessor;
		}
		throw new IllegalArgumentException("No processor available for " + event);
			
	}
}
