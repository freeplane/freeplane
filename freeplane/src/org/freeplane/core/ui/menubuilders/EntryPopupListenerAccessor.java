package org.freeplane.core.ui.menubuilders;

import java.util.ArrayList;
import java.util.Collection;

public class EntryPopupListenerAccessor {

	final private Collection<EntryPopupListener> listeners;
	final private Entry entry;

	public EntryPopupListenerAccessor(Entry entry) {
		this.entry = entry;
		final EntryPopupListenerAccessor registeredListener = (EntryPopupListenerAccessor) entry.getAttribute(EntryPopupListenerAccessor.class.getName());
		if(registeredListener == null){
			listeners = new ArrayList<EntryPopupListener>();
			entry.setAttribute(EntryPopupListenerAccessor.class.getName(), this);
		}
		else
			listeners = registeredListener.listeners;
	}

	public void addEntryPopupListener(EntryPopupListener entryPopupListener) {
		listeners.add(entryPopupListener);
	}

	public void popupMenuWillBecomeVisible() {
		for(EntryPopupListener entryPopupListener : listeners)
			entryPopupListener.popupWillBecomeVisible(entry);
		
	}

	public void popupMenuWillBecomeInvisible() {
		for(EntryPopupListener entryPopupListener : listeners)
			entryPopupListener.popupWillBecomeInvisible(entry);
	}

	public void popupMenuCanceled() {
		for(EntryPopupListener entryPopupListener : listeners)
			entryPopupListener.popupCanceled(entry);
	}

	public void removeEntryPopupListener(EntryPopupListener entryPopupListener) {
		listeners.remove(entryPopupListener);
	}

}
