package org.freeplane.features.mode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
	private static class ActionSelectorOnPropertyChange implements IFreeplanePropertyListener, IActionOnChange {
		final String propertyName;
		final AFreeplaneAction action;

		public ActionSelectorOnPropertyChange(final AFreeplaneAction action) {
			super();
			this.action = action;
			propertyName = action.getClass().getAnnotation(SelectableAction.class).checkOnPropertyChange();
		}

		public AFreeplaneAction getAction() {
			return action;
		}

		public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
			if (this.propertyName.equals(propertyName)) {
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
		final AFreeplaneAction old = actions.put(key, value);
		//String pattern = key.replaceAll("\\.", "\\\\.").replaceAll("/", "\\\\/"); 			
		//System.out.println("key\t\t" + value.getClass().getSimpleName() + "\t\ts/\\\"" + pattern + "\\\"/\\\"" + value.getClass().getSimpleName() + "\\\"/;");		
		if (old != null && !old.equals(value)) {
			actions.put(key, old);
			throw new RuntimeException("action " + key + " already registered");
		}
		if (value.checkSelectionOnPropertyChange()) {
			final ActionSelectorOnPropertyChange listener = new ActionSelectorOnPropertyChange(value);
			ResourceController.getResourceController().addPropertyChangeListener(listener);
		}
	}

	public AFreeplaneAction addActionIfNotAlreadySet(AFreeplaneAction action) {
		AFreeplaneAction existingAction = getAction(action.getKey());
        if(existingAction== null) {
            addAction(action);
            return action;
        } else {
            return existingAction;
        }
	}
	
	public AFreeplaneAction getAction(final String key) {
		return actions.get(key);
	}

	public Collection<AFreeplaneAction> getActions() {
		return actions.values();
	}
	
	public Set<String> getActionKeys(){
		return actions.keySet();
	}

	public AFreeplaneAction removeAction(final String key) {
		final AFreeplaneAction action = actions.remove(key);
		if (action.checkSelectionOnPropertyChange()) {
			ResourceController.getResourceController().removePropertyChangeListener(
			    ActionSelectorOnPropertyChange.class, action);
		}
		return action;
	}

	public AFreeplaneAction removeActionIfSet(final String key) {
		if(getAction(key) != null ){
			return removeAction(key);
		}
		else
			return null;
	}
	
}
