package org.freeplane.core.controller;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;

import org.freeplane.core.ui.AFreeplaneAction;

/**
 * Place for common controller things.
 * 
 * @author robert.ladstaetter
 */
public class AController {
	private final Map<String, Action> actions = new HashMap<String, Action>();

	public AController() {
	}

	public void addAction(final AFreeplaneAction a) {
		addAction(a.getName(), a);
	}

	public void addAction(final String key, final AFreeplaneAction value) {
		final Action old = getActions().put(key, value);
		if (old != null && !old.equals(value)) {
			getActions().put(key, old);
			throw new RuntimeException("action " + key + "already registered");
		}
	}

	public Action getAction(final String key) {
		return getActions().get(key);
	}

	protected Map<String, Action> getActions() {
		return actions;
	}

	public Action removeAction(final String key) {
		return getActions().remove(key);
	}
}
