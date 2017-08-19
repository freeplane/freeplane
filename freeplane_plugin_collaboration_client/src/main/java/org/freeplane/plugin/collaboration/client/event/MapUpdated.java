package org.freeplane.plugin.collaboration.client.event;

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

}
