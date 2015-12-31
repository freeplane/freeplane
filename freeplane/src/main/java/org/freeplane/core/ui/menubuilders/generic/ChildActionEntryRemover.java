package org.freeplane.core.ui.menubuilders.generic;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.FreeplaneActions;

public class ChildActionEntryRemover extends ChildEntryRemover{
	private final FreeplaneActions freeplaneActions;
	private final static EntryAccessor entryAccessor = new EntryAccessor();
	
	public ChildActionEntryRemover(FreeplaneActions freeplaneActions) {
		super();
		this.freeplaneActions = freeplaneActions;
	}

	@Override
	public void visit(Entry target) {
		unregisterActions(target);
		super.visit(target);
	}

	private void unregisterActions(Entry target) {
		final AFreeplaneAction action = entryAccessor.getAction(target);
		if(action != null)
			freeplaneActions.removeActionIfSet(action.getKey());
		for(Entry entry : target.children())
			unregisterActions(entry);
	}
}
