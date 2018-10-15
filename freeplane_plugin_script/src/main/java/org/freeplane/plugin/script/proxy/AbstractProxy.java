package org.freeplane.plugin.script.proxy;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.script.ScriptContext;

public abstract class AbstractProxy<T> {
	private static final String DEFAULT_CLASS_NAME_ENDING = "Proxy";
	private final T delegate;
	private final ScriptContext scriptContext;

	AbstractProxy(final T delegate, final ScriptContext scriptContext) {
		this.delegate = delegate;
		this.scriptContext = scriptContext;
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

	public ScriptContext getScriptContext() {
    	return scriptContext;
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
	    final String simpleName = getClass().getSimpleName();
		String className = simpleName.endsWith(DEFAULT_CLASS_NAME_ENDING) ? simpleName.substring(0, simpleName.length() - DEFAULT_CLASS_NAME_ENDING.length()) : simpleName;
		return className + ":" + delegate.toString();
    }
}
