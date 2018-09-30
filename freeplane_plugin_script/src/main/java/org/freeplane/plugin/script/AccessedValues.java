package org.freeplane.plugin.script;

import java.util.HashMap;
import java.util.Map;

import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.map.NodeModel;

class AccessedValues {
	private final Map<Object, String> accessedValues;

	AccessedValues() {
		this.accessedValues = new HashMap<Object, String>();
	}

	void accessAttribute(NodeModel node, Attribute attribute) {
		accessedValues.put(attribute, node.createID());
	}

	public void accessValue(NodeModel node) {
		final String id = node.createID();
		accessedValues.put(id, id);
	}
}