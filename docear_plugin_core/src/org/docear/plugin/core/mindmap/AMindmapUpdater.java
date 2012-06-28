package org.docear.plugin.core.mindmap;

import org.docear.plugin.core.features.MapModificationSession;
import org.freeplane.features.map.MapModel;

public abstract class AMindmapUpdater {
		
	private final String title;
	private MapModificationSession session;
	
	
	public AMindmapUpdater(String title) {	
		this.title = title;
	}

	public String getTitle() {
		return title;
	}	
	
	protected void setSession(MapModificationSession session) {
		this.session = session;		
	}
	
	protected Object getSessionObject(String key) {
		return session.getSessionObject(key);
	}
	
	protected void putSessionObject(String key, Object o) {
		session.putSessionObject(key, o);
	}
	/**
	 * update a node (e.g. references)
	 * @param node to be updated by this method
	 * @return <code>true</code> if any changes happened to the node during the execution of this method, else <code>false</code>
	 */	
	public abstract boolean updateMindmap(MapModel map);
	
	
}
