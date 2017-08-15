package org.freeplane.plugin.collaboration.client;

interface NodeUpdated extends MapUpdated {
	enum ContentType {
		CHILDREN, TEXT, NOTE, DETAILS, ATTRIBUTES, ICONS, OTHER_CONTENT, CLONES
	}
	
	String nodeId();
}
