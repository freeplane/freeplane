package org.freeplane.plugin.collaboration.client.event.batch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import javax.swing.Timer;
import javax.swing.event.EventListenerList;

import org.freeplane.plugin.collaboration.client.VisibleForTesting;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;

@SuppressWarnings("serial") 
public class Updates {
	private final class TimerExtension extends Timer {
		private TimerExtension(int delay, ActionListener listener) {
			super(delay, listener);
		}
		
		@Override
		protected void fireActionPerformed(ActionEvent e) {		
			builder = createBuilder();
			notifyListeners(e);
			registeredUpdates.clear();
			listenerList = new EventListenerList();
			UpdateBlockCompleted event = builder.build();
			builder = null;
			header.setMapRevision(header.mapRevision() + 1);
			consumer.onUpdates(event);
		}
		
		private void notifyListeners(ActionEvent e) {
	        Object[] listeners = listenerList.getListenerList();
	        for (int i=0; i<=listeners.length-2; i+=2) {
	            if (listeners[i]==ActionListener.class) {
	                ((ActionListener)listeners[i+1]).actionPerformed(e);
	            }
	            listeners = listenerList.getListenerList();
	        }
		}
	}
	
	private static class UpdateKey {
		private final Class<?> supplier;
		private final String elementId;
		UpdateKey(Class<?> supplier, String elementId) {
			super();
			this.supplier = supplier;
			this.elementId = elementId;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + elementId.hashCode();
			result = prime * result + supplier.hashCode();
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (getClass() != obj.getClass())
				return false;
			UpdateKey other = (UpdateKey) obj;
			if (!elementId.equals(other.elementId))
				return false;
			else if (!supplier.equals(other.supplier))
				return false;
			return true;
		}
		
	}

	final private ModifiableUpdateHeaderExtension header;
	final private UpdatesProcessor consumer;
	private ImmutableUpdateBlockCompleted.Builder builder;
	private final Timer timer;
	private final Set<UpdateKey> registeredUpdates;

	@VisibleForTesting
	public Updates(UpdatesProcessor consumer, int delay, ModifiableUpdateHeaderExtension header) {
		timer = new TimerExtension(delay, null);
		timer.setRepeats(false);
		registeredUpdates = new HashSet<>();
		this.consumer = consumer;
		this.header = header;
	}

	private ImmutableUpdateBlockCompleted.Builder createBuilder() {
		return UpdateBlockCompleted.builder()
				.mapId(header.mapId())
				.mapRevision(header.mapRevision() + 1);
	}

	public void addUpdateEvent(String updatedElementId, Supplier<MapUpdated> eventSupplier ) {
		if(registeredUpdates.add(new UpdateKey(eventSupplier.getClass(), updatedElementId)))
			timer.addActionListener(e -> builder.addUpdateBlock(eventSupplier.get()));
		timer.restart();
	}

	public void addOptionalUpdateEvent(String updatedElementId, Supplier<Optional<MapUpdated>> eventSupplier ) {
		if(registeredUpdates.add(new UpdateKey(eventSupplier.getClass(), updatedElementId)))
			timer.addActionListener(e -> eventSupplier.get().ifPresent(builder::addUpdateBlock));
		timer.restart();
	}

	public void addUpdateEvents(String updatedElementId, Supplier<MapUpdated[]> eventSupplier ) {
		if(registeredUpdates.add(new UpdateKey(eventSupplier.getClass(), updatedElementId)))
			timer.addActionListener(e -> builder.addUpdateBlock(eventSupplier.get()));
		timer.restart();
	}

	public void addUpdateEvents(String updatedElementId, Runnable eventSupplier) {
		if(registeredUpdates.add(new UpdateKey(eventSupplier.getClass(), updatedElementId)))
			timer.addActionListener(e -> eventSupplier.run());
		timer.restart();
	}
	public void addUpdateEvent(MapUpdated event) {
		builder.addUpdateBlock(event);
	}
	
	public void addUpdateEvents(MapUpdated... events) {
		builder.addUpdateBlock(events);
	}

	
}