package org.freeplane.plugin.collaboration.client.event;

import org.freeplane.plugin.collaboration.client.event.children.ImmutableChildrenUpdated;
import org.freeplane.plugin.collaboration.client.event.children.ImmutableRootNodeIdUpdated;
import org.freeplane.plugin.collaboration.client.event.children.ImmutableSpecialNodeTypeSet;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		  use = JsonTypeInfo.Id.NAME, 
		  include = JsonTypeInfo.As.PROPERTY, 
		  property = "contentType")
@JsonSubTypes({ 
		  @Type(value = ImmutableChildrenUpdated.class, name = "CHILDREN"), 
		  @Type(value = ImmutableRootNodeIdUpdated.class, name = "ROOT_ID"),
		  @Type(value = ImmutableSpecialNodeTypeSet.class, name = "SPECIAL_NODE"),
		})
public interface MapUpdated {
	// intentionally left blank
}
