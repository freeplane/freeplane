package org.docear.plugin.core.workspace.node.config;

import java.net.URI;
import java.util.HashSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class NodeAttributeObserver {
	private final HashSet<ChangeListener> listeners = new HashSet<ChangeListener>();
	private URI uri = null;

	public NodeAttributeObserver() {
		
	}
	
	public void addChangeListener(ChangeListener listener) {
		if(listener == null) {
			return;
		}
		this.listeners.clear();
		this.listeners.add(listener);
	}
	
	public void removeChangeListener(ChangeListener listener) {
		this.listeners.remove(listener);
	}
	
	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
		fireValueChanged();
	}

	private void fireValueChanged() {
		ChangeEvent event = new ChangeEvent(this);
		for(ChangeListener listener : listeners) {
 			listener.stateChanged(event);
		}		
	}
	
	public void reset() {
		this.uri = null;
	}
	
	
	
	
}
