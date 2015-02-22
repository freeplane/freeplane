package org.freeplane.core.ui.menubuilders;

import org.freeplane.core.ui.AFreeplaneAction;

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