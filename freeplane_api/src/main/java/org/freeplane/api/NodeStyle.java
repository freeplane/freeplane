package org.freeplane.api;

import java.awt.Color;

/** Node's style: <code>node.style</code> - read-write. */
public interface NodeStyle extends NodeStyleRO {
	/** Selects a style by name, see menu Styles &rarr; Pre/Userdefined styles for valid style names or use
	 * {@link #getName()} to display the name of a node's style.
	 * It's guaranteed that <code>node.style.name = node.style.name</code> does not change the style.
	 * @param styleName can be the name visible in the style menu or its translation key as returned by
	 *        {@link #getName()}. (Names of predefined styles are subject to translation.)
	 *        Only translation keys will continue to work if the language setting is changed.
	 * @throws IllegalArgumentException if the style does not exist.
	 * @since 1.2.2 */
	void setName(String styleName);

	void setBackgroundColor(Color color);

	/** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
	 *  @since 1.2 */
	void setBackgroundColorCode(String rgbString);

	/** @deprecated since 1.2 - use {@link #setTextColor(Color)} instead. */
	@Deprecated
	void setNodeTextColor(Color color);

	/** @since 1.2 */
	void setTextColor(Color color);

	/** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
	 *  @since 1.2 */
	void setTextColorCode(String rgbString);

    /** sets the floating style for the node (aka "free node"). Should normally only be applied to direct
     *  children of the root node.
     *  @since 1.2 */
    void setFloating(boolean floating);

    /** minNodeWidth in px - set to -1 to restore default.
     * @since 1.2.20 */
    void setMinNodeWidth(int width);

    /** use length units like "1 cm" or "6 pt"
     * @since 1.5.6 */
    void setMinNodeWidth(String width);

    /** minNodeWidth in px - set to -1 to restore default.
     * @since 1.2.20 */
    void setMaxNodeWidth(int width);

    /** use length units like "1 cm" or "6 pt"
     * @since 1.5.6 */
    void setMaxNodeWidth(String width);

    /** @since 1.3.8 */
    void setNumberingEnabled(boolean enabled);
}