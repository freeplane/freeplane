package org.freeplane.features.link;

import org.freeplane.features.map.NodeModel;

import java.util.*;

public class Connectors {
	public static final String CONNECTORS = "connectors";
	private final Set<ConnectorModel> connectors = new HashSet<>();
	private final Map<NodeModel, Collection<ConnectorModel>> connectorsFromSource = new HashMap<>();
	private final Map<NodeModel, Collection<ConnectorModel>> connectorsToTarget = new HashMap<>();

	public void add(ConnectorModel connector) {
		if (connectors.add(connector)) {
			connectorsFromSource.computeIfAbsent(connector.getSource(), n -> new ArrayList<>()).add(connector);
			connectorsToTarget.computeIfAbsent(connector.getSource(), n -> new ArrayList<>()).add(connector);
		}
	}

	public void addAll(Collection<? extends ConnectorModel> connectors) {
		connectors.stream().forEach(this::add);
	}

	public void clear() {
		connectorsFromSource.clear();
	}

	public boolean isHighlighted(ConnectorModel connector) {
		return connectors.contains(connector);
	}

	public Collection<ConnectorModel> getLinksFrom(NodeModel node){
		return connectorsFromSource.getOrDefault(node, (Collections.emptyList()));
	}

	public Collection<ConnectorModel> getLinksTo(NodeModel node){
		return connectorsToTarget.getOrDefault(node, (Collections.emptyList()));
	}

}
