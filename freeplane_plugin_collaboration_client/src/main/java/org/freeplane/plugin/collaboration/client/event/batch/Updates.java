package org.freeplane.plugin.collaboration.client.event.batch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

	final private ModifiableUpdateHeaderExtension header;
	final private UpdatesProcessor consumer;
	private ImmutableUpdateBlockCompleted.Builder builder;
	private final Timer timer;

	@VisibleForTesting
	public Updates(UpdatesProcessor consumer, int delay, ModifiableUpdateHeaderExtension header) {
		timer = new TimerExtension(delay, null);
		timer.setRepeats(false);
		this.consumer = consumer;
		this.header = header;
	}

	private ImmutableUpdateBlockCompleted.Builder createBuilder() {
		return UpdateBlockCompleted.builder()
				.mapId(header.mapId())
				.mapRevision(header.mapRevision() + 1);
	}


	
	
	public void addUpdateEvent(Supplier<MapUpdated> eventSupplier ) {
		timer.addActionListener(e -> builder.addUpdateBlock(eventSupplier.get()));
		timer.restart();
	}

	public void addUpdateEvents(Supplier<MapUpdated[]> eventSupplier ) {
		timer.addActionListener(e -> builder.addUpdateBlock(eventSupplier.get()));
		timer.restart();
	}

	public void addUpdateEvents(Runnable runnable) {
		timer.addActionListener(e -> runnable.run());
		timer.restart();
	}
	public void addUpdateEvent(MapUpdated event) {
		builder.addUpdateBlock(event);
	}
	
	public void addUpdateEvents(MapUpdated... events) {
		builder.addUpdateBlock(events);
	}

	
}