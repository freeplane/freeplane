package org.freeplane.core.ui.menubuilders;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class EntryPopupMenuListenerAccessor {

	final private Collection<PopupMenuListener> listeners;
	final private Entry entry;

	public EntryPopupMenuListenerAccessor(Entry entry) {
		this.entry = entry;
		final EntryPopupMenuListenerAccessor registeredListener = (EntryPopupMenuListenerAccessor) entry.getAttribute(EntryPopupMenuListenerAccessor.class.getName());
		if(registeredListener == null){
			listeners = new ArrayList<PopupMenuListener>();
			entry.setAttribute(EntryPopupMenuListenerAccessor.class.getName(), this);
		}
		else
			listeners = registeredListener.listeners;
	}

	public void addPopupMenuListener(PopupMenuListener popupMenuListener) {
		listeners.add(popupMenuListener);
	}

	public void popupMenuWillBecomeVisible() {
		final PopupMenuEvent event = new PopupMenuEvent(entry);
		for(PopupMenuListener popupMenuListener : listeners)
			popupMenuListener.popupMenuWillBecomeVisible(event);
		
	}

	public void popupMenuWillBecomeInvisible() {
		final PopupMenuEvent event = new PopupMenuEvent(entry);
		for(PopupMenuListener popupMenuListener : listeners)
			popupMenuListener.popupMenuWillBecomeInvisible(event);
	}

	public void popupMenuCanceled() {
		final PopupMenuEvent event = new PopupMenuEvent(entry);
		for(PopupMenuListener popupMenuListener : listeners)
			popupMenuListener.popupMenuCanceled(event);
	}

	public void removePopupMenuListener(PopupMenuListener popupMenuListener) {
		listeners.remove(popupMenuListener);
	}

}
