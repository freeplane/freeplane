package org.freeplane.features.nodestyle;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.nodestyle.NodeStyleModel.Shape;

public class ShapeConfigurationModel {
	final private NodeStyleModel.Shape shape;
	final private Quantity<LengthUnits> horizontalMargin;
	final private Quantity<LengthUnits> verticalMargin;
	final private  boolean isUniform;
	
	private ShapeConfigurationModel(final Shape shape, final Quantity<LengthUnits> horizontalMargin, final Quantity<LengthUnits> verticalMargin, final boolean isUniform) {
		super();
		this.shape = shape;
		this.horizontalMargin = horizontalMargin;
		this.verticalMargin = verticalMargin;
		this.isUniform = isUniform;
	}
	final static public Quantity<LengthUnits> DEFAULT_MARGIN = new Quantity<LengthUnits>(2, LengthUnits.pt);
	public static ShapeConfigurationModel EMTPY_SHAPE = new ShapeConfigurationModel(null, DEFAULT_MARGIN, DEFAULT_MARGIN, false);
	public static final ShapeConfigurationModel AS_PARENT = EMTPY_SHAPE.withShape(Shape.as_parent);
	public static final ShapeConfigurationModel FORK = EMTPY_SHAPE.withShape(Shape.fork);
	public NodeStyleModel.Shape getShape() {
		return shape;
	}
	public Quantity<LengthUnits> getHorizontalMargin() {
		return horizontalMargin;
	}
	public Quantity<LengthUnits> getVerticalMargin() {
		return verticalMargin;
	}
	public boolean isUniform() {
		return isUniform;
	}
	public ShapeConfigurationModel withShape(NodeStyleModel.Shape shape) {
		return new ShapeConfigurationModel(shape, horizontalMargin, verticalMargin, isUniform);
	}
	public ShapeConfigurationModel withHorizontalMargin(Quantity<LengthUnits> horizontalMargin) {
		return new ShapeConfigurationModel(shape, horizontalMargin, verticalMargin, isUniform);
	}
	public ShapeConfigurationModel withVerticalMargin(Quantity<LengthUnits> verticalMargin) {
		return new ShapeConfigurationModel(shape, horizontalMargin, verticalMargin, isUniform);
	}
	public ShapeConfigurationModel withUniform(boolean isUniform) {
		return new ShapeConfigurationModel(shape, horizontalMargin, verticalMargin, isUniform);
	}
	
}
