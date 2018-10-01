package org.freeplane.features.attribute;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class HighlighedAttributes {
	private final Set<Attribute> attributes = new HashSet<>();

	public void add(Attribute attribute) {
		attributes.add(attribute);
	}

	public void addAll(Collection<? extends Attribute> c) {
		attributes.addAll(c);
	}

	public void clear() {
		attributes.clear();
	}

	public boolean isHighlighted(Attribute attribute) {
		return attributes.contains(attribute);
	}
}
