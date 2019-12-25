package org.freeplane.core.ui.menubuilders.action;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.UserRole;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

public class ActionStatusUpdater implements EntryPopupListener {
	@Override
	public void childEntriesWillBecomeVisible(final Entry submenu) {
		MapModel map = Controller.getCurrentController().getMap();
		UserRole userRole = Controller.getCurrentModeController().userRole(map);
		childEntriesWillBecomeVisible(submenu, userRole);
	}

	void childEntriesWillBecomeVisible(final Entry submenu, UserRole userRole) {
		for (Entry target : submenu.children()) {
			final AFreeplaneAction action = new EntryAccessor().getAction(target);
			if (action != null) {
				if (action.checkEnabledOnPopup())
					action.setEnabled(userRole);
				if (action.checkSelectionOnPopup() && action.isEnabled())
					action.setSelected();
			}
		}
	}

	@Override
	public void childEntriesHidden(final Entry target) {
	}
}