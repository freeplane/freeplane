package org.freeplane.core.ui.menubuilders.action;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;

public class EntriesForAction {
	final Map<AFreeplaneAction, Collection<Entry>> entryMap = new HashMap<AFreeplaneAction, Collection<Entry>>();

    public void registerEntry(AFreeplaneAction action, Entry actionEntry) {
		final Collection<Entry> collection = entryMap.get(action);
		if (collection == null) {
			final LinkedList<Entry> list = new LinkedList<Entry>();
			list.add(actionEntry);
			entryMap.put(action, list);
		}
		else
			collection.add(actionEntry);
    }

    public void unregisterEntry(AFreeplaneAction action, Entry actionEntry) {
		final Collection<Entry> collection = entryMap.get(action);
		collection.remove(actionEntry);
		if (collection.isEmpty())
			entryMap.remove(action);
    }

	public Collection<Entry> entries(AFreeplaneAction action) {
		final Collection<Entry> collection = entryMap.get(action);
		return collection != null ? collection : Collections.<Entry> emptyList();
	}

    public boolean contains(AFreeplaneAction action) {
        return entryMap.containsKey(action);
    }
}