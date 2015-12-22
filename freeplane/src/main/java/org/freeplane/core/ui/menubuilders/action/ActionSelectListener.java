package org.freeplane.core.ui.menubuilders.action;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;

public class ActionSelectListener implements EntryPopupListener {
	public void childEntriesWillBecomeVisible(final Entry submenu) {
		for (Entry target : submenu.children()) {
			final AFreeplaneAction action = new EntryAccessor().getAction(target);
			if (action != null && action.checkSelectionOnPopup() && action.isEnabled())
				action.setSelected();
		}
	}

	public void childEntriesHidden(final Entry target) {
	}
}