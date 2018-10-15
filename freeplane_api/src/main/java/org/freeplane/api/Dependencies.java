package org.freeplane.api;

import java.util.List;

public class Dependencies {
	public enum Element{NODE}
	private final List<Element> elements;
	private final List<Integer> attributes;
	public Dependencies(List<Element> elements, List<Integer> attributes) {
		super();
		this.elements = elements;
		this.attributes = attributes;
	}
	public List<Element> getElements() {
		return elements;
	}
	public List<Integer> getAttributes() {
		return attributes;
	}

}
