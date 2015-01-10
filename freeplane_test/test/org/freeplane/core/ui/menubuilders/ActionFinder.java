package org.freeplane.core.ui.menubuilders;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.FreeplaneActions;

public class ActionFinder implements Builder{

	final private FreeplaneActions freeplaneActions;

	public ActionFinder(FreeplaneActions freeplaneActions) {
		this.freeplaneActions = freeplaneActions;
	}

	@Override
	public void build(Entry target) {
		final String actionName = target.getName();
		if(actionName != null) {
			final AFreeplaneAction action = freeplaneActions.getAction(actionName);
			target.setAction(action);
		}
	}

}
