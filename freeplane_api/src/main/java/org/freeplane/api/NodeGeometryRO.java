package org.freeplane.api;

/**@since 1.8.11 */
public interface NodeGeometryRO {
    NodeShape getShape();
    Quantity<LengthUnit> getHorizontalMargin();
    Quantity<LengthUnit> getVerticalMargin();
    boolean getIsUniform();
}
