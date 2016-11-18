package org.freeplane.features.highlight;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;

import org.freeplane.features.map.NodeModel;

public interface NodeHighlighter {
	public Stroke DEFAULT_STROKE = new BasicStroke(2.0f);
	public boolean isNodeHighlighted(NodeModel node);
	public void configure(Graphics2D g);
}
