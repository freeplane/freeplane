package org.freeplane.api;

import java.awt.Color;


/** Border to parent node: <code>node.style.border</code> - read-only. */
public interface BorderRO {

    Color getColor();

    String getColorCode();

    boolean isColorSet();

    Quantity<LengthUnit> getWidth();

}
