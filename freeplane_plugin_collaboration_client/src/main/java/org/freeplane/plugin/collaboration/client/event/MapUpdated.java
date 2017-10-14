package org.freeplane.plugin.collaboration.client.event;

import org.freeplane.plugin.collaboration.client.event.children.ImmutableChildrenUpdated;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		  use = JsonTypeInfo.Id.NAME, 
		  include = JsonTypeInfo.As.PROPERTY, 
		  property = "contentType")
@JsonSubTypes({ 
		  @Type(value = ImmutableChildrenUpdated.class, name = "CHILDREN"), 
		})
public interface MapUpdated {
	// intentionally left blank
}
