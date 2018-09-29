package org.freeplane.plugin.script;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.map.NodeModel;

class AccessedValues {
	private final Map<NodeModel, Set<Attribute>> accessedAttributes;

	AccessedValues() {
		this.accessedAttributes = new HashMap<NodeModel, Set<Attribute>>();
	}

	private Set<Attribute> writeableAttributesOf(NodeModel nodeModel) {
		Set<Attribute> attributes = accessedAttributes.get(nodeModel);
		if(attributes == null) {
			attributes = new HashSet<Attribute>();
			accessedAttributes.put(nodeModel, attributes);
		}
		return attributes;
	}

	public Set<NodeModel> getAccessedNodes() {
		return accessedAttributes != null ? accessedAttributes.keySet() : Collections.<NodeModel>emptySet();
	}
	public Set<Attribute> getAccessedAttributesOf(NodeModel node) {
		if(accessedAttributes == null)
			return Collections.emptySet();
		else {
			final Set<Attribute> attributes = accessedAttributes.get(node);
			if(attributes == null)
				return Collections.emptySet();
			else
				return attributes;
		}
	}

	void accessAttribute(NodeModel node, Attribute attribute) {
		writeableAttributesOf(node).add(attribute);
	}

	public void accessValue(NodeModel node) {
		if(! accessedAttributes.containsKey(node))
			accessedAttributes.put(node, null);
	}
}