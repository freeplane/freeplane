package org.freeplane.features.highlight;

import java.awt.Color;

import org.freeplane.features.map.NodeModel;

public interface NodeHighlighter {
	public boolean isNodeHighlighted(NodeModel node);
	public Color getColor();
}
