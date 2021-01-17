package org.freeplane.features.highlight;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.map.NodeModel;

public interface NodeHighlighter {
	public Stroke DEFAULT_STROKE = new BasicStroke(2.0f * UITools.FONT_SCALE_FACTOR);
	public boolean isNodeHighlighted(NodeModel node, boolean isPrinting);
	public void configure(Graphics2D g, boolean isPrinting);
}
