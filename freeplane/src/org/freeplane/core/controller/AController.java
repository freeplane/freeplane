package org.freeplane.core.controller;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;

import org.freeplane.core.actions.IFreeplaneAction;
import org.freeplane.core.extension.ExtensionContainer;

/**
 * Place for common controller things.
 * 
 * @author robert.ladstaetter
 */
public class AController extends ExtensionContainer {
	// TODO ARCH rladstaetter 18.02.2009 replace with Map<Class<? extends IFreeplaneAction>,IFreeplaneAction>
	private  final Map<String, Action> actions = new HashMap<String, Action>();

	public void putAction(IFreeplaneAction a) {
		getActions().put(a.getName(), a);
	}

	// TODO rladstaetter 15.02.2009 use put(FreeplaneAction a)
	@Deprecated
	public void putAction(final String key, final Action value) {
		getActions().put(key, value);
	}

	public Action getAction(final String key) {
		return getActions().get(key);
	}

	public Action removeAction(final String key) {
		return getActions().remove(key);
	}

	protected Map<String, Action> getActions() {
	    return actions;
    }

}
