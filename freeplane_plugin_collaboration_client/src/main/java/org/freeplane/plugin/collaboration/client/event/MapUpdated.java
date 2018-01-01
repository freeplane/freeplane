package org.freeplane.plugin.collaboration.client.event;

import org.freeplane.plugin.collaboration.client.event.children.ImmutableChildrenUpdated;
import org.freeplane.plugin.collaboration.client.event.children.ImmutableRootNodeIdUpdated;
import org.freeplane.plugin.collaboration.client.event.children.ImmutableSpecialNodeTypeSet;
import org.freeplane.plugin.collaboration.client.event.content.ImmutableCoreUpdated;
import org.freeplane.plugin.collaboration.client.event.content.ImmutableMapContentUpdated;
import org.freeplane.plugin.collaboration.client.event.content.ImmutableNodeContentUpdated;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		  use = JsonTypeInfo.Id.NAME, 
		  include = JsonTypeInfo.As.PROPERTY, 
		  property = "contentType")
@JsonSubTypes({ 
	  @Type(value = ImmutableChildrenUpdated.class, name = "CHILDREN"), 
	  @Type(value = ImmutableCoreUpdated.class, name = "CORE"),
	  @Type(value = ImmutableNodeContentUpdated.class, name = "OTHER_CONTENT"), 
	  @Type(value = ImmutableMapContentUpdated.class, name = "OTHER_MAP_CONTENT"), 
		  @Type(value = ImmutableRootNodeIdUpdated.class, name = "ROOT_ID"),
		  @Type(value = ImmutableSpecialNodeTypeSet.class, name = "SPECIAL_NODE"),
		})
public interface MapUpdated {
	enum ContentType {
		CHILDREN, TEXT, NOTE, DETAILS, ATTRIBUTES, ICONS, OTHER_CONTENT, CLONES
	}
	
	// intentionally left blank
}
