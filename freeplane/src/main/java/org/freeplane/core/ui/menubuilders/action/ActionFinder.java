package org.freeplane.core.ui.menubuilders.action;

import java.util.Arrays;

import org.freeplane.core.resources.SetBooleanPropertyAction;
import org.freeplane.core.resources.SetStringPropertyAction;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.FreeplaneActions;
import org.freeplane.features.styles.SetBooleanMapPropertyAction;
import org.freeplane.features.styles.SetBooleanMapViewPropertyAction;

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
			if(action == null) {
				for (final Class<? extends AFreeplaneAction> actionClass : Arrays.asList(SetBooleanPropertyAction.class, SetBooleanMapPropertyAction.class, SetBooleanMapViewPropertyAction.class,
					SetStringPropertyAction.class)){
					final String setBooleanPropertyActionPrefix = actionClass.getSimpleName() + ".";
					if (actionName.startsWith(setBooleanPropertyActionPrefix)) {
						String propertyName = actionName.substring(setBooleanPropertyActionPrefix.length());
						action = createAction(actionClass, propertyName);
						if(action !=  null) {
							freeplaneActions.addAction(action);
						}
						break;
					}
				}
			}
			
			new EntryAccessor().setAction(target, action);
		}
	}

	AFreeplaneAction createAction(
			Class<? extends AFreeplaneAction> actionClass, String propertyName) {
		try {
			return actionClass.getConstructor(String.class).newInstance(propertyName);
		} catch (Exception e) {
			LogUtils.severe(e);
			return null;
		}
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}

}
