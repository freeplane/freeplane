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

	protected final Map<String,Action> actionMap = new HashMap<String,Action>();

	public void putAction(IFreeplaneAction a) {
		actionMap.put(a.getName(),a);
	}
	
	// TODO rladstaetter 15.02.2009 use put(FreeplaneAction a)
	@Deprecated
	public void putAction(final String key, final Action value) {
		actionMap.put(key, value);
	}

	public Action getAction(final String key) {
		return actionMap.get(key);
	}

	
	public Action removeAction(final String key) {
		return actionMap.remove(key);
	}

}
