package org.freeplane.plugin.collaboration.client.event.content;

import java.util.Arrays;
import java.util.Collection;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.icon.HierarchicalIcons;
import org.freeplane.features.map.FirstGroupNodeFlag;
import org.freeplane.features.map.SummaryNodeFlag;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.plugin.collaboration.client.event.NodeUpdated;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableContentUpdated.class)
@JsonDeserialize(as = ImmutableContentUpdated.class)
public interface ContentUpdated extends NodeUpdated{
	
	static final Collection<Class<? extends IExtension>> EXCLUSIONS = 
			Arrays.asList(HierarchicalIcons.ACCUMULATED_ICONS_EXTENSION_CLASS, SummaryNodeFlag.class, FirstGroupNodeFlag.class,
					MapStyleModel.class);
	
	static ImmutableContentUpdated.Builder builder() {
		return ImmutableContentUpdated.builder();
	}
	
	String content();
}
