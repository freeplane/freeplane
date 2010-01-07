package org.freeplane.core.controller;

import java.util.HashMap;
import java.util.Map;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;

/**
 * Place for common controller things.
 * 
 * @author robert.ladstaetter
 */
public class AController {
	private static class ActionSelectorOnPropertyChange implements IFreeplanePropertyListener, IActionOnChange{
		final String propertyName;
		final AFreeplaneAction action;

		public ActionSelectorOnPropertyChange(final AFreeplaneAction action) {
			super();
			this.action = action;
			this.propertyName = action.getClass().getAnnotation(SelectableAction.class).checkOnPropertyChange();
		}

		public AFreeplaneAction getAction() {
			return action;
		}

		public void propertyChanged(String propertyName, String newValue, String oldValue) {
			if(this.propertyName.equals(propertyName)){
				action.setSelected();
			}
        }
	}

	public interface IActionOnChange {
		AFreeplaneAction getAction();
	}

	private final Map<String, AFreeplaneAction> actions = new HashMap<String, AFreeplaneAction>();

	public AController() {
	}

	public void addAction(final AFreeplaneAction value) {
		final String key = value.getKey();
		final AFreeplaneAction old = getActions().put(key, value);
		//String pattern = key.replaceAll("\\.", "\\\\.").replaceAll("/", "\\\\/"); 			
		//System.out.println("key\t\t" + value.getClass().getSimpleName() + "\t\ts/\\\"" + pattern + "\\\"/\\\"" + value.getClass().getSimpleName() + "\\\"/;");		
		if (old != null && !old.equals(value)) {
			getActions().put(key, old);
			throw new RuntimeException("action " + key + " already registered");
		}
		if (AFreeplaneAction.checkSelectionOnPropertyChange(value)) {
			final ActionSelectorOnPropertyChange listener = new ActionSelectorOnPropertyChange(value);
			ResourceController.getResourceController().addPropertyChangeListener(listener);
		}
	}

	public AFreeplaneAction getAction(final String key) {
		return getActions().get(key);
	}

	protected Map<String, AFreeplaneAction> getActions() {
		return actions;
	}

	public AFreeplaneAction removeAction(final String key) {
		final AFreeplaneAction action = getActions().remove(key);
		if (AFreeplaneAction.checkSelectionOnPropertyChange(action)) {
			ResourceController.getResourceController().removePropertyChangeListener(ActionSelectorOnPropertyChange.class, action);
		}
		return action;
	}
}
