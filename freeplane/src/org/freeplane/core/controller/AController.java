package org.freeplane.core.controller;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;

import org.freeplane.core.actions.IFreeplaneAction;

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

	public void putAction(final IFreeplaneAction a) {
		getActions().put(a.getName(), a);
	}

	// TODO rladstaetter 15.02.2009 use put(FreeplaneAction a)
	@Deprecated
	public void putAction(final String key, final Action value) {
		getActions().put(key, value);
	}

	public Action removeAction(final String key) {
		return getActions().remove(key);
	}
}
