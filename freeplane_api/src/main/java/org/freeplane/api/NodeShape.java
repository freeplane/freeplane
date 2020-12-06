package org.freeplane.api;

public enum NodeShape{FORK(false), BUBBLE, OVAL, RECTANGLE, WIDE_HEXAGON, NARROW_HEXAGON, AS_PARENT(false), COMBINED;
	public final boolean hasConfiguration;
	private final String lowerCaseName;

	private NodeShape() {
		this(true);
	}
	private NodeShape(boolean hasConfiguration) {
		this.hasConfiguration = hasConfiguration;
		this.lowerCaseName = name().toLowerCase();
	}
	
	public static NodeShape ignoreCaseValueOf(String name) {
	    return Enum.valueOf(NodeShape.class, name.toUpperCase());		
	}
	
	public String lowerCaseName() {
	    return lowerCaseName;
	}

}