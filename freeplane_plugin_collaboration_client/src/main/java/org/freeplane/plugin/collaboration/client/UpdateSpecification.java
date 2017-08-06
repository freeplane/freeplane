package org.freeplane.plugin.collaboration.client;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableUpdateSpecification.class)
@JsonDeserialize(as = ImmutableUpdateSpecification.class)
interface UpdateSpecification {
	enum ContentType {
		CHILDREN, TEXT, NOTE, DETAILS, ATTRIBUTES, ICONS, OTHER_CONTENT, CLONES
	}
	
	String nodeId();
	ContentType contentType();
	String content();
	
}
