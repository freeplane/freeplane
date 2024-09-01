package org.freeplane.api;

import java.awt.Color;



/** Edge to parent node: <code>node.style.edge</code> - read-only. */
public interface EdgeRO {
	Color getColor();

	String getColorCode();

	/**
	 * @since 1.11.8
	 */
	boolean isColorSet();

	EdgeStyle getType();

	/**
	 * @since 1.11.8
	 */
	boolean isTypeSet();

	int getWidth();

	/**
	 * @since 1.11.8
	 */
	boolean isWidthSet();

	/**
	 * @since 1.11.8
	 */
    Dash getDash();

	/**
	 * @since 1.11.8
	 */
    boolean isDashSet();
}