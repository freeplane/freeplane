package org.freeplane.core.ui.menubuilders.action;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.UserRole;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class ActionStatusUpdater implements EntryPopupListener {
	@Override
	public void childEntriesWillBecomeVisible(final Entry submenu) {
		UserRole userRole = currentUserRole();
		childEntriesWillBecomeVisible(submenu, userRole);
	}

	UserRole currentUserRole() {
		Controller currentController = Controller.getCurrentController();
		if(currentController == null)
			return UserRole.ADVANCED_EDITOR;
		ModeController currentModeController = Controller.getCurrentModeController();
		if(currentModeController == null)
			return UserRole.ADVANCED_EDITOR;
		
		MapModel map = currentController.getMap();
		UserRole userRole = currentModeController.userRole(map);
		return userRole;
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