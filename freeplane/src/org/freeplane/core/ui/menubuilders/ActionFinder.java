package org.freeplane.core.ui.menubuilders;

import static java.lang.Boolean.TRUE;
import static org.freeplane.core.ui.menubuilders.RecursiveMenuStructureProcessor.PROCESS_ON_POPUP;

import org.freeplane.core.resources.SetBooleanPropertyAction;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.FreeplaneActions;

public class ActionFinder implements EntryVisitor{

	final private FreeplaneActions freeplaneActions;

	public ActionFinder(FreeplaneActions freeplaneActions) {
		this.freeplaneActions = freeplaneActions;
	}

	@Override
	public void visit(final Entry target) {
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

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return TRUE.equals(entry.getAttribute(PROCESS_ON_POPUP));
	}

}
