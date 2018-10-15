package org.freeplane.api;

import java.util.List;
/**
 * Contains dependencies (precedent or descendent node or attributes)
 * calculated by {@link DependencyLookup}
 */
public class Dependencies {
	public enum Element{NODE}
	private final List<Element> elements;
	private final List<Integer> attributes;
	public Dependencies(List<Element> elements, List<Integer> attributes) {
		super();
		this.elements = elements;
		this.attributes = attributes;
	}

	/**
	 * Returns a list of related {@link Element}s
	 */
	public List<Element> getElements() {
		return elements;
	}

	/**
	 * Returns a list of related attribute indices
	 */
	public List<Integer> getAttributes() {
		return attributes;
	}

	@Override
	public String toString() {
		return "[" + elements + "," + attributes + "]";
	}



}
