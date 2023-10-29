package org.freeplane.api;

import java.awt.Color;


/**
*
* Border to parent node: <code>node.style.border</code> - read-only.
*
* @since 1.11.8
*
*/
public interface BorderRO {

    Color getColor();

    String getColorCode();

    boolean isColorSet();

    Quantity<LengthUnit> getWidth();

    boolean isWidthSet();

    Dash getDash();

    boolean isDashSet();

    boolean getUsesEdgeColor();

    boolean getUsesEdgeWidth();

    boolean getUsesEdgeDash();

    boolean isUsesEdgeColorSet();

    boolean isUsesEdgeWidthSet();

    boolean isUsesEdgeDashSet();

}
