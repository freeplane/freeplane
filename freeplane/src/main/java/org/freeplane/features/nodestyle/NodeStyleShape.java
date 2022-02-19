package org.freeplane.features.nodestyle;

public enum NodeStyleShape{fork(false), bubble, oval, rectangle, wide_hexagon, narrow_hexagon, as_parent(false), combined;
	public final boolean hasConfiguration;

	private NodeStyleShape() {
		this(true);
	}
	private NodeStyleShape(boolean hasConfiguration) {
		this.hasConfiguration = hasConfiguration;
	}

}