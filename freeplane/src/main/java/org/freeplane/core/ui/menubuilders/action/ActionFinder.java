package org.freeplane.core.ui.menubuilders.action;

import org.freeplane.core.resources.SetBooleanPropertyAction;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.features.mode.FreeplaneActions;

public class ActionFinder implements EntryVisitor{

	final private FreeplaneActions freeplaneActions;

	public ActionFinder(FreeplaneActions freeplaneActions) {
		this.freeplaneActions = freeplaneActions;
	}

	@Override
	public void visit(final Entry target) {
		final String actionName = target.getName();
		if (!actionName.isEmpty() && new EntryAccessor().getAction(target) == null) {
			AFreeplaneAction action = freeplaneActions.getAction(actionName);
			final String setBooleanPropertyActionPrefix = SetBooleanPropertyAction.class.getSimpleName() + ".";
			if(action == null && actionName.startsWith(setBooleanPropertyActionPrefix)){
				String propertyName = actionName.substring(setBooleanPropertyActionPrefix.length());
				action = createSetBooleanPropertyAction(propertyName);
				freeplaneActions.addAction(action);
			}
			
			new EntryAccessor().setAction(target, action);
		}
	}

	protected SetBooleanPropertyAction createSetBooleanPropertyAction(
			String propertyName) {
		return new SetBooleanPropertyAction(propertyName);
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}

}
