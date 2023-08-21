package org.freeplane.api;

import java.awt.Color;


/** Border to parent node: <code>node.style.border</code> - read-write. */
public interface Border extends BorderRO {
    void setColor(Color color);

    /** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
     *  @since 1.2 */
    void setColorCode(String rgbString);

}
