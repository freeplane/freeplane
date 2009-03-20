package org.freeplane.core.controller;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;

import org.freeplane.core.ui.IFreeplaneAction;

/**
 * Place for common controller things.
 * 
 * @author robert.ladstaetter
 */
public class AController {
	// TODO ARCH rladstaetter 18.02.2009 replace with Map<Class<? extends IFreeplaneAction>,IFreeplaneAction>
	private final Map<String, Action> actions = new HashMap<String, Action>();

	public AController() {
	}

	public Action getAction(final String key) {
		return getActions().get(key);
	}

	protected Map<String, Action> getActions() {
		return actions;
	}

	public void addAction(final IFreeplaneAction a) {
		addAction(a.getName(), a);
	}

	// TODO rladstaetter 15.02.2009 use addAction(IFreeplaneAction a)
	@Deprecated
	public void addAction(final String key, final Action value) {
		Action old = getActions().put(key, value);
		if(old != null && ! old.equals(value)){
			getActions().put(key, old);
			throw new RuntimeException("action " + key + "already registered");
		}
	}

	public Action removeAction(final String key) {
		return getActions().remove(key);
	}
}
