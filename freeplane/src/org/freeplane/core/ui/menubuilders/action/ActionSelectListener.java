package org.freeplane.core.ui.menubuilders.action;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;

class ActionSelectListener implements
		EntryPopupListener {
	public void childEntriesWillBecomeVisible(final Entry target) {
		final AFreeplaneAction action = target.getAction();
		if(action.checkSelectionOnPopup() && action.isEnabled())
			action.setSelected();
	}

	public void childEntriesWillBecomeInvisible(final Entry target) {
	}
}