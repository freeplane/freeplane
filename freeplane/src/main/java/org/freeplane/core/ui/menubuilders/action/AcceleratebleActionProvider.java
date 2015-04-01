package org.freeplane.core.ui.menubuilders.action;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.AccelerateableAction;
import org.freeplane.core.ui.IFreeplaneAction;
import org.freeplane.features.mode.Controller;

public class AcceleratebleActionProvider {
	public IFreeplaneAction wrap(final AFreeplaneAction action) {
		if (isApplet()) {
			return action;
		}
		return acceleratableAction(action);
	}

	protected boolean isApplet() {
		return Controller.getCurrentController().getViewController().isApplet();
	}

	public IFreeplaneAction acceleratableAction(final AFreeplaneAction action) {
		return new AccelerateableAction(action);
	}
}
