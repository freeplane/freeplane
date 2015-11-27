package org.freeplane.features.nodestyle;

import org.freeplane.features.nodestyle.NodeStyleModel.Shape;

public class ShapeConfigurationModel {
	final private NodeStyleModel.Shape shape;
	final private int horizontalMargin;
	final private int verticalMargin;
	final private  boolean isUniform;
	
	private ShapeConfigurationModel(Shape shape, int horizontalMargin, int verticalMargin, boolean isUniform) {
		super();
		this.shape = shape;
		this.horizontalMargin = horizontalMargin;
		this.verticalMargin = verticalMargin;
		this.isUniform = isUniform;
	}
	final static public int DEFAULT_MARGIN = 3;
	public static ShapeConfigurationModel EMTPY_SHAPE = new ShapeConfigurationModel(null, DEFAULT_MARGIN, DEFAULT_MARGIN, false);
	public NodeStyleModel.Shape getShape() {
		return shape;
	}
	public int getHorizontalMargin() {
		return horizontalMargin;
	}
	public int getVerticalMargin() {
		return verticalMargin;
	}
	public boolean isUniform() {
		return isUniform;
	}
	public ShapeConfigurationModel withShape(NodeStyleModel.Shape shape) {
		return new ShapeConfigurationModel(shape, horizontalMargin, verticalMargin, isUniform);
	}
	public ShapeConfigurationModel withHorizontalMargin(int horizontalMargin) {
		return new ShapeConfigurationModel(shape, horizontalMargin, verticalMargin, isUniform);
	}
	public ShapeConfigurationModel withVerticalMargin(int verticalMargin) {
		return new ShapeConfigurationModel(shape, horizontalMargin, verticalMargin, isUniform);
	}
	public ShapeConfigurationModel withUniform(boolean isUniform) {
		return new ShapeConfigurationModel(shape, horizontalMargin, verticalMargin, isUniform);
	}
	
}
