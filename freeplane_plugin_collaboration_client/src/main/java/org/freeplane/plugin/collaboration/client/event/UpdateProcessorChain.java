package org.freeplane.plugin.collaboration.client.event;

import java.util.HashMap;
import java.util.List;

import org.freeplane.collaboration.event.MapUpdated;
import org.freeplane.core.undo.SelectionActor;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

public class UpdateProcessorChain{

	private final Controller controller;

	public UpdateProcessorChain(Controller controller) {
		super();
		this.controller = controller;
	}


	private final HashMap<Class<? extends MapUpdated>, UpdateProcessor<? extends MapUpdated> > processors = new HashMap<>();

	public <T extends MapUpdated> UpdateProcessorChain add(UpdateProcessor<T> processor) {
		processors.put(processor.eventClass(), processor);
		return this;
	}


	void onUpdate(MapModel map, MapUpdated event) {
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
		final SelectionActor selectionActor = map == controller.getMap() ? SelectionActor.createForActOnly(controller.getSelection()) : null;

		for (MapUpdated event : events)
			onUpdate(map, event);

		if(selectionActor != null)
			controller.getModeController().execute(selectionActor, map);
	}
}
