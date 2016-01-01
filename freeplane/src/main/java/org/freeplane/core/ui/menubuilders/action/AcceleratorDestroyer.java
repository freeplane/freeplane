package org.freeplane.core.ui.menubuilders.action;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;

public class AcceleratorDestroyer implements EntryVisitor{

	private final IEntriesForAction entries;

	public AcceleratorDestroyer(IEntriesForAction entries) {
		this.entries = entries;
	}

	public void visit(Entry entry) {
		final AFreeplaneAction action = new EntryAccessor().getAction(entry);
		if (action != null) {
			entries.unregisterEntry(action, entry);
		}
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}

}
