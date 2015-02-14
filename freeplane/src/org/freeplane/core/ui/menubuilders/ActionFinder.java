package org.freeplane.core.ui.menubuilders;

import org.freeplane.core.resources.SetBooleanPropertyAction;
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
			AFreeplaneAction action = freeplaneActions.getAction(actionName);
			final String setBooleanPropertyActionPrefix = SetBooleanPropertyAction.class.getSimpleName() + ".";
			if(action == null && actionName.startsWith(setBooleanPropertyActionPrefix)){
				String propertyName = actionName.substring(setBooleanPropertyActionPrefix.length());
				action = createSetBooleanPropertyAction(propertyName);
				freeplaneActions.addAction(action);
			}
			target.setAction(action);
		}
	}

	protected SetBooleanPropertyAction createSetBooleanPropertyAction(
			String propertyName) {
		return new SetBooleanPropertyAction(propertyName);
	}

}
