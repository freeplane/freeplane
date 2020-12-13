package org.freeplane.features.nodestyle;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.features.nodestyle.NodeStyleModel.Shape;

public class NodeGeometryModel {
	final private NodeStyleModel.Shape shape;
	final private Quantity<LengthUnit> horizontalMargin;
	final private Quantity<LengthUnit> verticalMargin;
	final private  boolean isUniform;
	
	private NodeGeometryModel(final Shape shape, final Quantity<LengthUnit> horizontalMargin, final Quantity<LengthUnit> verticalMargin, final boolean isUniform) {
		super();
		this.shape = shape;
		this.horizontalMargin = horizontalMargin;
		this.verticalMargin = verticalMargin;
		this.isUniform = isUniform;
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((horizontalMargin == null) ? 0 : horizontalMargin.hashCode());
		result = prime * result + (isUniform ? 1231 : 1237);
		result = prime * result + ((shape == null) ? 0 : shape.hashCode());
		result = prime * result + ((verticalMargin == null) ? 0 : verticalMargin.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeGeometryModel other = (NodeGeometryModel) obj;
		if (shape != other.shape)
			return false;
		if (isUniform != other.isUniform)
			return false;
		if (!horizontalMargin.equals(other.horizontalMargin))
			return false;
		if (!verticalMargin.equals(other.verticalMargin))
			return false;
		return true;
	}



	final static public Quantity<LengthUnit> DEFAULT_MARGIN = new Quantity<LengthUnit>(2, LengthUnit.pt);
	public static NodeGeometryModel NULL_SHAPE = new NodeGeometryModel(null, DEFAULT_MARGIN, DEFAULT_MARGIN, false);
	public static final NodeGeometryModel AS_PARENT = NULL_SHAPE.withShape(Shape.as_parent);
	public static final NodeGeometryModel FORK = NULL_SHAPE.withShape(Shape.fork);
	private static final Quantity<LengthUnit> DEFAULT_HORIZONTAL_OVAL_MARGIN = new Quantity<LengthUnit>(6, LengthUnit.pt);
	private static final Quantity<LengthUnit> DEFAULT_VERTICAL_OVAL_MARGIN = new Quantity<LengthUnit>(12, LengthUnit.pt);
	public static final NodeGeometryModel DEFAULT_ROOT_OVAL = NULL_SHAPE.withShape(Shape.oval)
			.withHorizontalMargin(DEFAULT_HORIZONTAL_OVAL_MARGIN).withVerticalMargin(DEFAULT_VERTICAL_OVAL_MARGIN);
	public NodeStyleModel.Shape getShape() {
		return shape;
	}
	public Quantity<LengthUnit> getHorizontalMargin() {
		return horizontalMargin;
	}
	public Quantity<LengthUnit> getVerticalMargin() {
		return verticalMargin;
	}
	public boolean isUniform() {
		return isUniform;
	}
	public NodeGeometryModel withShape(NodeStyleModel.Shape shape) {
		return new NodeGeometryModel(shape, horizontalMargin, verticalMargin, isUniform);
	}
	public NodeGeometryModel withHorizontalMargin(Quantity<LengthUnit> horizontalMargin) {
		return new NodeGeometryModel(shape, horizontalMargin, verticalMargin, isUniform);
	}
	public NodeGeometryModel withVerticalMargin(Quantity<LengthUnit> verticalMargin) {
		return new NodeGeometryModel(shape, horizontalMargin, verticalMargin, isUniform);
	}
	public NodeGeometryModel withUniform(boolean isUniform) {
		return new NodeGeometryModel(shape, horizontalMargin, verticalMargin, isUniform);
	}
	
}
