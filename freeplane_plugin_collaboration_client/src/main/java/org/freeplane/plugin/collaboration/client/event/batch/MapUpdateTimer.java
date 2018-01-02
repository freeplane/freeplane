package org.freeplane.plugin.collaboration.client.event.batch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;
import javax.swing.event.EventListenerList;

import org.freeplane.plugin.collaboration.client.VisibleForTesting;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;

@SuppressWarnings("serial") 
public class MapUpdateTimer extends Timer {
	final private ModifiableUpdateHeaderExtension header;
	final private UpdatesProcessor consumer;
	private ImmutableUpdateBlockCompleted.Builder builder;

	@VisibleForTesting
	public MapUpdateTimer(UpdatesProcessor consumer, int delay, ModifiableUpdateHeaderExtension header) {
		super(delay, null);
		setRepeats(false);
		this.consumer = consumer;
		this.header = header;
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
	
	private ImmutableUpdateBlockCompleted.Builder createBuilder() {
		return UpdateBlockCompleted.builder()
				.mapId(header.mapId())
				.mapRevision(header.mapRevision() + 1);
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
	
	public void addUpdateBlock(MapUpdated event) {
		builder.addUpdateBlock(event);
	}
	
	public void addUpdateBlock(MapUpdated... events) {
		builder.addUpdateBlock(events);
	}
	
}