package org.freeplane.api;

import java.awt.Color;


/**
 *
 * Border to parent node: <code>node.style.border</code> - read-write.
 *
 * @since 1.11.8
 *
 */
public interface Border extends BorderRO {
    void setColor(Color color);

    void setColorCode(String rgbString);

    void setWidth(Quantity<LengthUnit> borderWidth);

    void setDash(Dash dash);

    void setUsesEdgeColor(Boolean borderColorMatchesEdgeColor);

    void setUsesEdgeWidth(Boolean borderColorMatchesEdgeColor);

    void setUsesEdgeDash(Boolean borderColorMatchesEdgeColor);

}
