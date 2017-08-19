package org.freeplane.plugin.collaboration.client.event;

public interface NodeUpdated extends MapUpdated {
	enum ContentType {
		CHILDREN, TEXT, NOTE, DETAILS, ATTRIBUTES, ICONS, OTHER_CONTENT, CLONES
	}
	
	String nodeId();
}
