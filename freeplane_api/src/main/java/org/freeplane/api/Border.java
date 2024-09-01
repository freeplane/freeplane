package org.freeplane.api;

import java.awt.Color;


/**
 *
 * Border to parent node: <code>node.style.border</code> - read-write.
 *
 * @since 1.11.8
 *
 * <p>
 * <b>Examples:</b>
 * <pre>
 *
 * //imports are needed for example node 'A --------------- A'
 * import org.freeplane.api.Quantity
 * import org.freeplane.api.LengthUnit
 *
 *
 * node.createChild('A --------------- A').style.border.with{
 *     // defining border width using Quantity and LengthUnit
 *     width = Quantity.fromString('6', LengthUnit.px)
 *     usesEdgeWidth = false
 *     usesEdgeDash = false
 *     dash = 'DASHES'
 *     usesEdgeColor = false
 *     colorCode = '#cc00ff'
 * }
 *
 * node.createChild('B --------------- B').style.border.with{
 *     //width defined directly in px
 *     width = 4
 *     usesEdgeWidth = false
 *     usesEdgeDash = true
 *     dash = 'DISTANT_DOTS'
 *     usesEdgeColor = false
 *     colorCode = '#00ffcc'
 * }
 *
 * node.createChild('C --------------- C').style.border.with{
 *     //using a string to define width's value and unit
 *     width = '2 mm'
 *     usesEdgeWidth = false
 *     usesEdgeDash = false
 *     dash = 'DOTS_AND_DASHES'
 *     usesEdgeColor = true
 *     colorCode = '#ffcc00'
 * }
 *
 * </pre>
 */
public interface Border extends BorderRO {
    void setColor(Color color);

    void setColorCode(String rgbString);

    void setWidth(Quantity<LengthUnit> borderWidth);

    /** sets the border's width in pixels
     */
    void setWidth(Integer borderWidth);

    /** sets the border's width using a string to define its value and LengthUnit
     *
     * @param borderWidth string with format "number unit"<br>
     *                    examples: "2 px", "3.2 mm"<br>
     *
     * example to get all possible units:
     *                    <pre>node.createChild( org.freeplane.api.LengthUnit.values().join(', ') )</pre>
     */
    void setWidth(String borderWidth);

    /**
     *
     * @param dash any of these: 'SOLID', 'CLOSE_DOTS', 'DASHES', 'DISTANT_DOTS', 'DOTS_AND_DASHES'
     *
     * <br> example to get all possible line types:
     * <pre>node.createChild( org.freeplane.api.Dash.values().join(', ') )</pre>
     */
    void setDash(Dash dash);

    void setUsesEdgeColor(Boolean borderColorMatchesEdgeColor);

    void setUsesEdgeWidth(Boolean borderColorMatchesEdgeColor);

    void setUsesEdgeDash(Boolean borderColorMatchesEdgeColor);

}
