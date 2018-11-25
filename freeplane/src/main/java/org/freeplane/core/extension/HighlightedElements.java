package org.freeplane.core.extension;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class HighlightedElements  implements IExtension {

	public static HighlightedElements of(Configurable configurable) {
		return configurable.computeIfAbsent(HighlightedElements.class, HighlightedElements::new);
	}

	private final Set<Object> elements = new HashSet<>();

	public void add(Object element) {
		elements.add(element);
	}

	public void addAll(Collection<?> c) {
		elements.addAll(c);
	}

	public void clear() {
		elements.clear();
	}

	public boolean isContained(Object element) {
		return elements.contains(element);
	}

	public Set<Object> getElements() {
		return elements;
	}

	public Stream<Object> stream() {
		return elements.stream();
	}
}
