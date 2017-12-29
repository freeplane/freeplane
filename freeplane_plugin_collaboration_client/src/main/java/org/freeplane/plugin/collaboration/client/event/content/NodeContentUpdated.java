package org.freeplane.plugin.collaboration.client.event.content;

import java.util.Collection;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.mode.MapExtensions;
import org.freeplane.plugin.collaboration.client.event.NodeUpdated;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableNodeContentUpdated.class)
@JsonDeserialize(as = ImmutableNodeContentUpdated.class)
public interface NodeContentUpdated extends NodeUpdated{
	
	static final Collection<Class<? extends IExtension>> EXCLUSIONS = MapExtensions.getAll();
	
	static ImmutableNodeContentUpdated.Builder builder() {
		return ImmutableNodeContentUpdated.builder();
	}
	
	String content();
}
