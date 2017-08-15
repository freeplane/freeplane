package org.freeplane.plugin.collaboration.client;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@JsonTypeInfo(
		  use = JsonTypeInfo.Id.NAME, 
		  include = JsonTypeInfo.As.PROPERTY, 
		  property = "contentType")
		@JsonSubTypes({ 
		  @Type(value = ImmutableChildrenUpdated.class, name = "CHILDREN"), 
		})
public interface MapUpdated {

}
