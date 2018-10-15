package org.freeplane.plugin.script.dependencies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.freeplane.api.Dependencies;
import org.freeplane.api.Dependencies.Element;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;

public class DependenciesBuilder {
	private final NodeAttributeTableModel attributes;

	public DependenciesBuilder(NodeAttributeTableModel attributes) {
		super();
		this.attributes = attributes;
	}

	private boolean isNodeContained = false;
	private ArrayList<Integer> attributeList;

	public void setNodeContained() {
		isNodeContained = true;
	}

	public void addAttribute(Attribute attribute) {
		if(attributeList == null)
			attributeList = new ArrayList<>();
		int attributeIndex = attributes.getAttributeIndex(attribute);
		if(attributeIndex < 0)
			throw new IllegalArgumentException("Attribute not found");
		attributeList.add(attributeIndex);
	}

	public Dependencies build() {
		final List<Element> elements = isNodeContained ? Collections.singletonList(Dependencies.Element.NODE) : Collections.emptyList();
		return new Dependencies(elements, attributeList != null ? attributeList : Collections.emptyList());
	}
}