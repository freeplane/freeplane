package org.docear.plugin.core.mindmap;

import org.freeplane.features.map.NodeModel;

public abstract class AMindmapUpdater {
		
	private final String title;
	
	public AMindmapUpdater(String title) {	
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public abstract void updateNode(NodeModel node);
}
