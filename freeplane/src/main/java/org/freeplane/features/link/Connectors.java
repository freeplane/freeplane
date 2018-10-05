package org.freeplane.features.link;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.freeplane.core.extension.Configurable;
import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

public class Connectors implements IExtension {
	static Connectors of(Configurable configurable){
		return configurable.computeIfAbsent(Connectors.class, Connectors::new);
	}

	private final Map<NodeModel, Collection<ConnectorModel>> connectorsFromSource = new HashMap<>();
	private final Map<NodeModel, Collection<ConnectorModel>> connectorsToTarget = new HashMap<>();

	public void add(ConnectorModel connector) {
		connectorsFromSource.computeIfAbsent(connector.getSource(), n -> new ArrayList<>()).add(connector);
		connectorsToTarget.computeIfAbsent(connector.getTarget(), n -> new ArrayList<>()).add(connector);
	}

	public void addAll(Collection<? extends ConnectorModel> connectors) {
		connectors.stream().forEach(this::add);
	}

	public void clear() {
		connectorsFromSource.clear();
	}

	public Collection<ConnectorModel> getLinksFrom(NodeModel node){
		return connectorsFromSource.getOrDefault(node, (Collections.emptyList()));
	}

	public Collection<ConnectorModel> getLinksTo(NodeModel node){
		return connectorsToTarget.getOrDefault(node, (Collections.emptyList()));
	}

}
