package org.docear.plugin.core.workspace.node.config;

import java.util.HashSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class NodeAttributeObserver {
	private final HashSet<ChangeListener> listeners = new HashSet<ChangeListener>();
	private Object value = null;

	public NodeAttributeObserver() {
		
	}
	
	public void addChangeListener(ChangeListener listener) {
		if(listener == null) {
			return;
		}
		this.listeners.add(listener);
	}
	
	public void removeChangeListener(ChangeListener listener) {
		this.listeners.remove(listener);
	}
	
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
		fireValueChanged();
	}

	private void fireValueChanged() {
		ChangeEvent event = new ChangeEvent(this);
		for(ChangeListener listener : listeners) {
			listener.stateChanged(event);
		}
		
	}
	
	
	
	
}
