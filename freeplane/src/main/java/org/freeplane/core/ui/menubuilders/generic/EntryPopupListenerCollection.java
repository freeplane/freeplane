package org.freeplane.core.ui.menubuilders.generic;

import java.util.ArrayList;
import java.util.Collection;

public class EntryPopupListenerCollection implements EntryPopupListener {

	final private Collection<EntryPopupListener> listeners;

	public EntryPopupListenerCollection() {
			listeners = new ArrayList<EntryPopupListener>();
	}

	public void addEntryPopupListener(EntryPopupListener entryPopupListener) {
		listeners.add(entryPopupListener);
	}

	public void childEntriesWillBecomeVisible(Entry entry) {
		for(EntryPopupListener entryPopupListener : listeners)
			entryPopupListener.childEntriesWillBecomeVisible(entry);
		
	}

	public void childEntriesWillBecomeInvisible(Entry entry) {
		for(EntryPopupListener entryPopupListener : listeners)
			entryPopupListener.childEntriesWillBecomeInvisible(entry);
	}

	public void removeEntryPopupListener(EntryPopupListener entryPopupListener) {
		listeners.remove(entryPopupListener);
	}

}
