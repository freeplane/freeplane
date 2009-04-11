package org.freeplane.core.controller;

import java.util.HashMap;
import java.util.Map;

import org.freeplane.core.ui.AFreeplaneAction;

/**
 * Place for common controller things.
 * 
 * @author robert.ladstaetter
 */
public class AController {
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
			throw new RuntimeException("action " + key + "already registered");
		}
	}

	public AFreeplaneAction getAction(final String key) {
		return getActions().get(key);
	}

	protected Map<String, AFreeplaneAction> getActions() {
		return actions;
	}

	public AFreeplaneAction removeAction(final String key) {
		return getActions().remove(key);
	}
}
