package org.freeplane.api;

import java.awt.Color;


/**
*
* Border to parent node: <code>node.style.border</code> - read-only.
*
* @since 1.11.8
*
 * <p>
 * <b>Example:</b>
 * <pre>
 * def msg = ""
 * msg += "this node has border line type '$node.style.border.dash' set"
 * msg += node.style.border.usesEdgeDash?',\nbut actually it shows the same line type as the node\'s edge':''
 * ui.informationMessage(msg)
 *
 * </pre>
 *
 * <b>Example:</b>
 * <pre>
 * def myText = new StringBuilder('|parameter|value|is set?|\n|:---|:---:|:---:|\n')
 *
 * node.style.border.with{
 *     myText << '|width|' + width + '|' + widthSet  + '|\n'
 *            << '|usesEdgeWidth|' + usesEdgeWidth  + '|' + usesEdgeWidthSet  + '|\n'
 *            << '|usesEdgeDash|' + usesEdgeDash  + '|' + usesEdgeDashSet  + '|\n'
 *            << '|dash|' + dash  + '|' + dashSet  + '|\n'
 *            << '|usesEdgeColor|' + usesEdgeColor  + '|' + usesEdgeColorSet  + '|\n'
 *            << '|color|' + color + ' ( ' + colorCode + ' )|' + colorSet  + '|\n'
 * }
 *
 * def outputNode = node.createChild('border parameters in node\'s note')
 * outputNode.note = myText.toString()
 * outputNode.noteContentType = 'markdown'
 *
 * </pre>
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
