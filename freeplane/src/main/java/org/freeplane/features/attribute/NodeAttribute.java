package org.freeplane.features.attribute;

import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.map.NodeModel;

public class NodeAttribute {
	public final NodeModel node;
	public final Attribute attribute;

	public NodeAttribute(NodeModel node, Attribute attribute) {
		this.node = node;
		this.attribute = attribute;
	}

	public String name() {
		return attribute.getName();
	}

	public Object value() {
		return attribute.getValue();
	}
}
