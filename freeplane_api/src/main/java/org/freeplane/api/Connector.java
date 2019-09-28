package org.freeplane.api;

import java.awt.Color;
import java.util.List;



/** Graphical connector between nodes:<code>node.connectorsIn</code> / <code>node.connectorsOut</code>
 * - read-write. */
public interface Connector extends ConnectorRO {
    /** @param shape one of LINE, LINEAR_PATH, CUBIC_CURVE, EDGE_LIKE.
     *  @since 1.3 */
    void setShape(String shape);

    void setColor(Color color);

	/** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
	 *  @since 1.2 */
	void setColorCode(String rgbString);

	/** @since 1.2 */
	void setEndArrow(boolean showArrow);

	void setMiddleLabel(String label);

	void setSimulatesEdge(boolean simulatesEdge);

	void setSourceLabel(String label);

    /** @since 1.2 */
    void setStartArrow(boolean showArrow);

	void setTargetLabel(String label);

    /** startPoint, endPoint: list of two integers representing a Point.
     * @since 1.3.3 */
    void setInclination(final List<Integer> startPoint, final List<Integer> endPoint);
    
	/**
	 * Since 1.7.10
	 */
	void setDashArray(int[] dashArray);
	
	/**
	 * 0 <= opacity <= 255
	 * 
	 * Since 1.7.10
	 */
	void setOpacity(int opacity);
	
	/**
	 * Since 1.7.10
	 */
	void setWidth(int width);
	
	
	/**
	 * Since 1.7.10
	 */
	void setLabelFontFamily(String name);
	
	/**
	 * Since 1.7.10
	 */
	void setLabelFontSize(int size);


}