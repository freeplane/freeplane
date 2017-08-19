package org.freeplane.plugin.collaboration.client.event.batch;

import java.awt.event.ActionEvent;

import javax.swing.Timer;
import javax.swing.event.EventListenerList;

@SuppressWarnings("serial") 
class OneTimeTimer extends Timer {
	OneTimeTimer(int delay) {
		super(delay, null);
		setRepeats(false);
	}

	@Override
	protected void fireActionPerformed(ActionEvent e) {
		super.fireActionPerformed(e);
		listenerList = new EventListenerList();
	}
}