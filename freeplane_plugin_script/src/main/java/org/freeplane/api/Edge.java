package org.freeplane.api;

import java.awt.Color;

import org.freeplane.features.edge.EdgeStyle;

/** Edge to parent node: <code>node.style.edge</code> - read-write. */
public interface Edge extends EdgeRO {
	void setColor(Color color);

	/** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
	 *  @since 1.2 */
	void setColorCode(String rgbString);

	void setType(EdgeStyle type);

	/** can be -1 for default, 0 for thin, &gt;0 */
	void setWidth(int width);
}