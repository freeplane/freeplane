package org.freeplane.plugin.collaboration.client.event;

import org.freeplane.plugin.collaboration.client.event.children.ImmutableNodeInserted;
import org.freeplane.plugin.collaboration.client.event.children.ImmutableNodeMoved;
import org.freeplane.plugin.collaboration.client.event.children.ImmutableNodeRemoved;
import org.freeplane.plugin.collaboration.client.event.children.ImmutableRootNodeIdUpdated;
import org.freeplane.plugin.collaboration.client.event.children.ImmutableSpecialNodeTypeSet;
import org.freeplane.plugin.collaboration.client.event.content.core.ImmutableCoreUpdated;
import org.freeplane.plugin.collaboration.client.event.content.links.ImmutableConnectorAdded;
import org.freeplane.plugin.collaboration.client.event.content.links.ImmutableConnectorRemoved;
import org.freeplane.plugin.collaboration.client.event.content.links.ImmutableConnectorUpdated;
import org.freeplane.plugin.collaboration.client.event.content.links.ImmutableHyperlinkUpdated;
import org.freeplane.plugin.collaboration.client.event.content.other.ImmutableMapContentUpdated;
import org.freeplane.plugin.collaboration.client.event.content.other.ImmutableNodeContentUpdated;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "contentType")
@JsonSubTypes({ @Type(value = ImmutableNodeInserted.class, name = "INSERTED"),
        @Type(value = ImmutableNodeMoved.class, name = "MOVED"),
        @Type(value = ImmutableNodeRemoved.class, name = "REMOVED"),
        @Type(value = ImmutableCoreUpdated.class, name = "CORE"),
        @Type(value = ImmutableNodeContentUpdated.class, name = "OTHER_CONTENT"),
        @Type(value = ImmutableMapContentUpdated.class, name = "OTHER_MAP_CONTENT"),
        @Type(value = ImmutableRootNodeIdUpdated.class, name = "ROOT_ID"),
        @Type(value = ImmutableSpecialNodeTypeSet.class, name = "SPECIAL_NODE"),
        @Type(value = ImmutableConnectorAdded.class, name = "CONNECTOR_ADDED"),
        @Type(value = ImmutableConnectorUpdated.class, name = "CONNECTOR_UPDATED"),
        @Type(value = ImmutableConnectorRemoved.class, name = "CONNECTOR_REMOVED"),
        @Type(value = ImmutableHyperlinkUpdated.class, name = "HYPERLINK_UPDATED"), })
public interface MapUpdated {
	enum ContentType {
		CHILDREN, TEXT, NOTE, DETAILS, ATTRIBUTES, ICONS, OTHER_CONTENT, CLONES
	}
	// intentionally left blank
}
