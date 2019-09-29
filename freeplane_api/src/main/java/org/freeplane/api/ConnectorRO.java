package org.freeplane.api;

import java.awt.Color;
import java.util.List;



/** Graphical connector between nodes:<code>node.connectorsIn</code> / <code>node.connectorsOut</code>
 * - read-only. */
public interface ConnectorRO {
    /** returns one of LINE, LINEAR_PATH, CUBIC_CURVE, EDGE_LIKE.
     *  @since 1.3 */
    String getShape();

    Color getColor();

	String getColorCode();
	
	/**
	 * Since 1.7.10
	 */
	int[] getDashArray();
	
	/**
	 * 0 <= opacity <= 255
	 * 
	 * Since 1.7.10
	 */
	int getOpacity();
	
	/**
	 * Since 1.7.10
	 */
	int getWidth();
	
	
	/**
	 * Since 1.7.10
	 */
	String getLabelFontFamily();
	
	/**
	 * Since 1.7.10
	 */
	int getLabelFontSize();

    /**  @since 1.2 */
	boolean hasEndArrow();

	String getMiddleLabel();

	/** The node without the arrow. On connectors with arrows at both ends one of the ends. */
	Node getSource();

	String getSourceLabel();

    /** @since 1.2 */
	boolean hasStartArrow();

	/** The node with the arrow. On connectors with arrows at both ends one of the ends. */
	Node getTarget();

	String getTargetLabel();

	boolean simulatesEdge();

	/** returns a Point.
	 * @since 1.3.3 */
	List<Integer> getStartInclination();

	/** returns a Point.
	 * @since 1.3.3 */
	List<Integer> getEndInclination();
}