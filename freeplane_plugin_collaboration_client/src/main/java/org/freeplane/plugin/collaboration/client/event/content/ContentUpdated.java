package org.freeplane.plugin.collaboration.client.event.content;

import java.util.Collection;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.mode.MapExtensions;
import org.freeplane.plugin.collaboration.client.event.NodeUpdated;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableContentUpdated.class)
@JsonDeserialize(as = ImmutableContentUpdated.class)
public interface ContentUpdated extends NodeUpdated{
	
	static final Collection<Class<? extends IExtension>> EXCLUSIONS = MapExtensions.getAll();
	
	static ImmutableContentUpdated.Builder builder() {
		return ImmutableContentUpdated.builder();
	}
	
	String content();
}
