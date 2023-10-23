package org.freeplane.api;

import java.awt.Color;
import java.util.List;



/** Node's style: <code>node.style</code> - read-only. */
public interface NodeStyleRO {

	/** Returns the name of the node's style if set or null otherwise. For styles with translated names the
	 * translation key is returned to make the process robust against language setting changes.
	 * It's guaranteed that <code>node.style.name = node.style.name</code> does not change the style.
	 * @since 1.2.2 */
	String getName();

	/**
	 *
	 * Returns all style names/translation keys active for the node.
	 *
	 * @see getName()
	 *
	 * @since 1.9.8
	 */
	List<String> getAllActiveStyles();

	Node getStyleNode();

	Color getBackgroundColor();

	/** returns HTML color spec like #ff0000 (red) or #222222 (darkgray).
	 *  @since 1.2 */
	String getBackgroundColorCode();

	Edge getEdge();

	Border getBorder();

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

    /** @since 1.9.12 */
    String getCss();

    /** @since 1.9.12 */
    boolean isCssSet();

    /** @since 1.9.12 */
    boolean isBackgroundColorSet();
    /** @since 1.9.12 */
    boolean isTextColorSet();
    /** @since 1.9.12 */
    boolean isMinNodeWidthSet();
    /** @since 1.9.12 */
    boolean isMaxNodeWidthSet();
    /** @since 1.11.8 */
    boolean isHorizontalTextAlignmentSet();
    /** @since 1.11.8 */
    HorizontalTextAlignment getHorizontalTextAlignment();

}