package org.freeplane.features.styles;

import org.freeplane.features.map.NodeModel;

public class StyleNode implements IStyle {
	final private NodeModel node;

	public NodeModel getNode() {
    	return node;
    }

	public StyleNode(NodeModel node) {
	    super();
	    this.node = node;
    }

	@Override
    public String toString() {
	    return node.toString();
    }
	
	
}
