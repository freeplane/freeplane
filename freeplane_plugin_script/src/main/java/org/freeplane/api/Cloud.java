package org.freeplane.api;

import java.awt.Color;

/** Here are four ways to enable a cloud on the current node and switch it off again:
 * <pre>
 *   node.cloud.enabled = true
 *   node.cloud.enabled = false
 *
 *   node.cloud.shape = 'ROUND_RECT' // either 'ARC', 'STAR', 'RECT' or 'ROUND_RECT'
 *   node.cloud.shape = null
 *
 *   node.cloud.color = java.awt.Color.YELLOW
 *   node.cloud.color = null
 *
 *   node.cloud.colorCode = '#00FF66'
 *   node.cloud.color = null
 * </pre>
 * @since 1.3 */
public interface Cloud {
    /**  @since 1.3 */
    boolean getEnabled();
    /**  @since 1.3 */
    void setEnabled(boolean enable);

    /** @return either null (if cloud is not enabled), "ARC", "STAR", "RECT" or "ROUND_RECT".
     *  @since 1.3 */
    String getShape();
    /** @param shape use "ARC", "STAR", "RECT" or "ROUND_RECT". null removes the cloud
     *  @since 1.3 */
    void setShape(String shape);

    /** @return either null (if cloud is not enabled) or the current cloud color.
     * @since 1.3 */
    Color getColor();
    /** @since 1.3 */
    void setColor(Color color);

    /** @return either null (if cloud is not enabled) or a HTML color spec.
     *  @since 1.3 */
    String getColorCode();
    /** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
     *  @since 1.3 */
    void setColorCode(String rgbString);
}