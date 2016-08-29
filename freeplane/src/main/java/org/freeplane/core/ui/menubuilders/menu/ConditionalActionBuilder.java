package org.freeplane.core.ui.menubuilders.menu;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.FreeplaneActions;

public class ConditionalActionBuilder implements EntryVisitor {

	private FreeplaneActions freeplaneActions;

	public ConditionalActionBuilder(FreeplaneActions freeplaneActions) {
		this.freeplaneActions = freeplaneActions;
	}

	@Override
	public void visit(Entry target) {
		final String property = (String)target.getAttribute("property");
		if(ResourceController.getResourceController().getBooleanProperty(property, false)) {
			try {
				final String keyName = (String) target.getAttribute("actionKey");
				final AFreeplaneAction existingAction = freeplaneActions.getAction(keyName);
				final AFreeplaneAction action;
				if(existingAction != null)
					action = existingAction;
				else {
					final String className = (String) target.getAttribute("actionClass");
					final Class<?> classDefinition = getClass().getClassLoader().loadClass(className);
					action = (AFreeplaneAction) classDefinition.newInstance();
					freeplaneActions.addAction(action);
				}
				new EntryAccessor().setAction(target, action);
				return;
			} catch (Exception e) {
				LogUtils.severe(e);
			}
		}

	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}

}
