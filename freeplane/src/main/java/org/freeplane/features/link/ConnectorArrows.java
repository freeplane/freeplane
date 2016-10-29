package org.freeplane.features.link;


public enum ConnectorArrows {
	 NONE(ArrowType.NONE, ArrowType.NONE), 
	 FORWARD(ArrowType.NONE, ArrowType.DEFAULT), 
	 BACKWARD(ArrowType.DEFAULT, ArrowType.NONE), 
	 BOTH(ArrowType.DEFAULT, ArrowType.DEFAULT);
	
	 private ConnectorArrows(ArrowType start, ArrowType end) {
		this.start = start;
		this.end = end;
	}
	public final ArrowType start;
	public final ArrowType end;
}
