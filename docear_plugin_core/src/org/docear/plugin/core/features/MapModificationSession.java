package org.docear.plugin.core.features;

import java.util.HashMap;

public class MapModificationSession {
	final private HashMap<String, Object> session = new HashMap<String, Object>();
	
	public Object getSessionObject(String key) {
		return session.get(key);
	}
	
	public void putSessionObject(String key, Object o) {
		this.session.put(key, o);
	}

}
