package org.freeplane.plugin.script.proxy;

import org.freeplane.core.model.NodeModel;
import org.freeplane.features.mindmapmode.MModeController;

public abstract class AbstractProxy {
	final private Object delegate;
	final private MModeController modeController;
	
	AbstractProxy(final Object delegate, final MModeController modeController){
		this.delegate = delegate;
		this.modeController = modeController;
	}
	public Object getDelegate() {
		return delegate;
	}
	
	public NodeModel getNode() {
		return (NodeModel) delegate;
	}

	public MModeController getModeController() {
		return modeController;
	}
	@Override
	public boolean equals(Object obj) {
		if(! getClass().equals(obj.getClass())){
			return false;
		}
		return delegate.equals(((AbstractProxy)obj).getDelegate());
	}

	@Override
	public int hashCode() {
		return delegate.hashCode() * 31 + getClass().hashCode();
	}

}
