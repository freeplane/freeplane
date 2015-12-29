package org.freeplane.core.ui.menubuilders.generic;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.ModeController;

public class ChildActionEntryRemover extends ChildEntryRemover{
	private final ModeController modeController;
	private final static EntryAccessor entryAccessor = new EntryAccessor();
	
	public ChildActionEntryRemover(ModeController modeController) {
		super();
		this.modeController = modeController;
	}

	@Override
	public void visit(Entry target) {
		unregisterActions(target);
		super.visit(target);
	}

	private void unregisterActions(Entry target) {
		final AFreeplaneAction action = entryAccessor.getAction(target);
		if(action != null)
			modeController.removeActionIfSet(action.getKey());
		for(Entry entry : target.children())
			unregisterActions(entry);
	}
}
