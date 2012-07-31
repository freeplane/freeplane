package org.docear.plugin.core.features;

import java.util.HashMap;

public class MapModificationSession {
	public static final String FILE_IGNORE_LIST = "file_ignore_list";
	public static final String URL_IGNORE_LIST = "url_ignore_list";
	
	final private HashMap<String, Object> session = new HashMap<String, Object>();
	
	public Object getSessionObject(String key) {
		return session.get(key);
	}
	
	public void putSessionObject(String key, Object o) {
		this.session.put(key, o);
	}

}
