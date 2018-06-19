package org.freeplane.api;

import java.awt.Color;



/** Edge to parent node: <code>node.style.edge</code> - read-only. */
public interface EdgeRO {
	Color getColor();

	String getColorCode();

	EdgeStyle getType();

	int getWidth();
}