package org.freeplane.api;

import java.awt.Color;
import java.util.List;

import org.freeplane.features.link.ArrowType;

/** Graphical connector between nodes:<code>node.connectorsIn</code> / <code>node.connectorsOut</code>
 * - read-only. */
public interface ConnectorRO {
    /** returns one of LINE, LINEAR_PATH, CUBIC_CURVE, EDGE_LIKE.
     *  @since 1.3 */
    String getShape();

    Color getColor();

	String getColorCode();

    /**  @since 1.2 */
	boolean hasEndArrow();

	/**@deprecated since 1.2 - use {@link #hasEndArrow()} instead */
	@Deprecated
	ArrowType getEndArrow();

	String getMiddleLabel();

	/** The node without the arrow. On connectors with arrows at both ends one of the ends. */
	Node getSource();

	String getSourceLabel();

    /** @since 1.2 */
	boolean hasStartArrow();

	/** @deprecated since 1.2 - use {@link #hasStartArrow()} instead */
	@Deprecated
	ArrowType getStartArrow();

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