package org.freeplane.core.ui.menubuilders.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class EntryPopupListenerCollection implements EntryPopupListener {

	final private List<EntryPopupListener> listeners;

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

	public void childEntriesHidden(Entry entry) {
	    ListIterator<EntryPopupListener> reverseIterator = listeners.listIterator(listeners.size());
		while(reverseIterator.hasPrevious())
			reverseIterator.previous().childEntriesHidden(entry);
	}

	public void removeEntryPopupListener(EntryPopupListener entryPopupListener) {
		listeners.remove(entryPopupListener);
	}

}
