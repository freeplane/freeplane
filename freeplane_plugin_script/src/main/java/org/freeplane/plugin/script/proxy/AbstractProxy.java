package org.freeplane.plugin.script.proxy;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.script.ScriptExecution;

public abstract class AbstractProxy<T> {
	private final T delegate;
	private final ScriptExecution scriptExecution;

	AbstractProxy(final T delegate, final ScriptExecution scriptExecution) {
		this.delegate = delegate;
		this.scriptExecution = scriptExecution;
	}

	@SuppressWarnings("rawtypes")
    @Override
	public boolean equals(final Object obj) {
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		return delegate.equals(((AbstractProxy) obj).getDelegate());
	}

	public T getDelegate() {
		return delegate;
	}

	public ScriptExecution getScriptExecution() {
    	return scriptExecution;
    }

	public MModeController getModeController() {
		return (MModeController) Controller.getCurrentModeController();
	}

	@Override
	public int hashCode() {
		return delegate.hashCode() * 31 + getClass().hashCode();
	}

	@Override
    public String toString() {
	    return getClass() + ":" + delegate.toString();
    }
}
