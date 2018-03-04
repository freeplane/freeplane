package org.freeplane.plugin.collaboration.client.event.batch;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.swing.Timer;
import javax.swing.event.EventListenerList;

import org.freeplane.collaboration.event.MapUpdated;
import org.freeplane.core.extension.IExtension;
import org.freeplane.plugin.collaboration.client.VisibleForTesting;

@SuppressWarnings("serial")
public class Updates implements IExtension{
	private final class TimerExtension extends Timer {
		private ActionEvent currentEvent;

		private TimerExtension(int delay, ActionListener listener) {
			super(delay, listener);
		}

		@Override
		protected void fireActionPerformed(ActionEvent e) {
			this.currentEvent = e;
			try {
				eventList = new ArrayList<>();
				notifyListeners(e);
				registeredUpdates.clear();
				listenerList = new EventListenerList();
				List<MapUpdated> consumedEvents = eventList;
				eventList = null;
				consumer.onUpdates(consumedEvents);
			}
			finally {
				this.currentEvent = null;
			}
		}		

		private void notifyListeners(ActionEvent e) {
			Object[] listeners = listenerList.getListenerList();
			for (int i = 0; i <= listeners.length - 2; i += 2) {
				if (listeners[i] == ActionListener.class) {
					((ActionListener) listeners[i + 1]).actionPerformed(e);
				}
			}
		}

		@Override
		public void addActionListener(ActionListener listener) {
			if (currentEvent != null)
				listener.actionPerformed(currentEvent);
			else
				super.addActionListener(listener);
		}

		public void runNow() {
			ActionEvent event = new ActionEvent(this, 0, getActionCommand(),
				System.currentTimeMillis(),
				0);
			if(! EventQueue.isDispatchThread())
				try {
					EventQueue.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							fireActionPerformed(event);
						}
					});
				}
				catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			else {
				fireActionPerformed(event);
			}
			stop();
			
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

	private List<MapUpdated> eventList;

	final private UpdatesProcessor consumer;
	private final TimerExtension timer;
	private final Set<UpdateKey> registeredUpdates;

	private int mapRevision;

	@VisibleForTesting
	public Updates(UpdatesProcessor consumer, int delay, int mapRevision) {
		this.mapRevision = mapRevision;
		timer = new TimerExtension(delay, null);
		timer.setRepeats(false);
		registeredUpdates = new HashSet<>();
		this.consumer = consumer;
	}

	public void addUpdateEvent(String updatedElementId, Supplier<MapUpdated> eventSupplier) {
		if (registeredUpdates.add(new UpdateKey(eventSupplier.getClass(), updatedElementId)))
			addUpdateEvent(eventSupplier);
		else
			timer.restart();
	}

	public void addUpdateEvent(Supplier<MapUpdated> eventSupplier) {
		timer.addActionListener(e -> eventList.add(eventSupplier.get()));
		timer.restart();
	}

	public void addUpdateEvents(String updatedElementId, Runnable eventSupplier) {
		if (registeredUpdates.add(new UpdateKey(eventSupplier.getClass(), updatedElementId)))
			addUpdateEvents(eventSupplier);
		else
			timer.restart();
	}

	public void addUpdateEvents(Runnable eventSupplier) {
		timer.addActionListener(e -> eventSupplier.run());
		timer.restart();
	}

	public void addUpdateEvent(MapUpdated event) {
		eventList.add(event);
	}
	
	public void flush() {
		timer.runNow();
	}

	public void incrementMapRevision() {
		mapRevision++;
	}

	public int mapRevision() {
		return mapRevision;
	}
}
