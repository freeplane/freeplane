package org.freeplane.plugin.script;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.map.NodeModel;

public class AccessedValues {
	private final Map<Object, NodeModel> accessedValues;
	private final NodeModel accessingNode;

	AccessedValues(NodeModel accessingNode) {
		this.accessingNode = accessingNode;
		this.accessedValues = new HashMap<Object, NodeModel>();
	}

	void accessAttribute(NodeModel accessedNode, Attribute attribute) {
		if(accessingNode.getMap() == accessedNode.getMap())
			accessedValues.put(attribute, accessedNode);
	}

	public void accessValue(NodeModel accessedNode) {
		if(accessingNode.getMap() == accessedNode.getMap()) {
			final String id = accessedNode.createID();
			accessedValues.put(accessedNode, accessedNode);
		}
	}

	public Collection<NodeModel> getAccessedNodes() {
		return accessedValues.values();
	}

	public Collection<Object> getAccessedValues() {
		return accessedValues.keySet();
	}
}
