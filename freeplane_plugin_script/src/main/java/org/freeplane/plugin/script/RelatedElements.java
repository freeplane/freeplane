package org.freeplane.plugin.script;

import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.map.NodeModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RelatedElements {
	private final Map<Object, NodeModel> relatedElements;
	private final NodeModel relatedNode;

	public RelatedElements(final NodeModel relatedNode) {
		this.relatedNode = relatedNode;
		relatedElements = new HashMap<Object, NodeModel>();
	}

	public void relateAttribute(final NodeModel relatedNode, final Attribute attribute) {
		if (this.relatedNode.getMap() == relatedNode.getMap())
			relatedElements.put(attribute, relatedNode);
	}

	public void relateNode(final NodeModel accessedNode) {
		if (relatedNode.getMap() == accessedNode.getMap()) {
			relatedElements.put(accessedNode, accessedNode);
		}
	}

	public boolean isEmpty() {
		return relatedElements.isEmpty();
	}

	public Collection<NodeModel> getRelatedNodes() {
		return relatedElements.values();
	}

	public Collection<Object> getElements() {
		return relatedElements.keySet();
	}
}
