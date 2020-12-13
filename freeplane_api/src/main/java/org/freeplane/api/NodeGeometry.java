package org.freeplane.api;

/**@since 1.8.11 */
public interface NodeGeometry extends NodeGeometryRO {
    void setShape(NodeShape shape);
    void setHorizontalMargin(Quantity<LengthUnit> length);
    void setVerticalMargin(Quantity<LengthUnit> length);
    void setIsUniform(boolean isUniform);
}
