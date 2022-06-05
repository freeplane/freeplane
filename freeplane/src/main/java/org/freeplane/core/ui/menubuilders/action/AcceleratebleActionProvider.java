package org.freeplane.core.ui.menubuilders.action;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.AccelerateableAction;
import org.freeplane.core.ui.IFreeplaneAction;
import org.freeplane.core.util.Compat;

public class AcceleratebleActionProvider {
	public IFreeplaneAction wrap(final AFreeplaneAction action) {
		if (Compat.isApplet()) {
			return action;
		}
		return acceleratableAction(action);
	}

	public IFreeplaneAction acceleratableAction(final AFreeplaneAction action) {
		return new AccelerateableAction(action);
	}
}
