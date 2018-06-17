package org.freeplane.api;

import java.awt.Color;

import org.freeplane.features.styles.IStyle;

/** Node's style: <code>node.style</code> - read-only. */
public interface NodeStyleRO {
	IStyle getStyle();

	/** Returns the name of the node's style if set or null otherwise. For styles with translated names the
	 * translation key is returned to make the process robust against language setting changes.
	 * It's guaranteed that <code>node.style.name = node.style.name</code> does not change the style.
	 * @since 1.2.2 */
	String getName();

	Node getStyleNode();

	Color getBackgroundColor();

	/** returns HTML color spec like #ff0000 (red) or #222222 (darkgray).
	 *  @since 1.2 */
	String getBackgroundColorCode();

	Edge getEdge();

	Font getFont();

	/** @deprecated since 1.2 - use {@link #getTextColor()} instead. */
	@Deprecated
	Color getNodeTextColor();

	/** @since 1.2 */
	Color getTextColor();

	String getTextColorCode();

    /** @since 1.2 true if the floating style is set for the node (aka "free node"). */
    boolean isFloating();

    /** @since 1.2.20 */
    int getMinNodeWidth();

    /** @since 1.2.20 */
    int getMaxNodeWidth();

    /** @since 1.3.8 */
    boolean isNumberingEnabled();
}