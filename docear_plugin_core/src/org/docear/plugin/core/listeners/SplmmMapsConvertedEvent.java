package org.docear.plugin.core.listeners;

public class SplmmMapsConvertedEvent {

	private Object object;
	
	public SplmmMapsConvertedEvent(Object object) {
		this.object = object;
	}
	
	public Object getObject() {
		return this.object;
	}
}
