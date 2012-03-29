package org.freeplane.view.swing.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

interface Drawable{
	void draw(Graphics2D g, NodeView nodeView, Point p);
}

class DrawableNothing implements Drawable{
	public void draw(Graphics2D g, NodeView nodeView, Point p) {
	}
}
abstract class DrawableShape implements Drawable{
	private final Color fillColor;

	public DrawableShape(Color fillColor) {
		this.fillColor = fillColor;
	}
	public void draw(Graphics2D g, NodeView nodeView, Point p) {
		final Color color = g.getColor(); 
		final Color edgeColor = nodeView.getEdgeColor();
		int shapeWidth = getWidth(nodeView, p);
		final Shape shape = getShape(p.x, p.y, shapeWidth);
		g.setColor(fillColor);
		g.fill(shape);
		g.setColor(edgeColor);
		g.draw(shape);
		g.setColor(color);
	}
	protected int getWidth(NodeView nodeView, Point p) {
		int zoomedFoldingSymbolHalfWidth = nodeView.getMainView().getZoomedFoldingSymbolHalfWidth();
		p.translate(-zoomedFoldingSymbolHalfWidth, -zoomedFoldingSymbolHalfWidth);
		int shapeWidth = zoomedFoldingSymbolHalfWidth * 2;
		return shapeWidth;
	}
	
	abstract Shape getShape(int x, int y, int width);
}

class DrawableEllipse extends DrawableShape{
	public DrawableEllipse(Color fillColor) {
		super(fillColor);
	}
	Shape getShape(int x, int y, int width){
		return new Ellipse2D.Float(x, y, width, width);
	}
}

class DrawableTriangle extends DrawableShape{
	public DrawableTriangle(Color fillColor) {
		super(fillColor);
	}
	Shape getShape(int x, int y, int width){
		final Polygon polygon = new Polygon();
		polygon.addPoint(x, y);
		polygon.addPoint(x + width, y);
		polygon.addPoint(x + width / 2, y + width*2/3);
		polygon.addPoint(x, y);
		return polygon;
	}
}

enum FoldingMark implements Drawable{
	UNFOLDED(new DrawableNothing()), ITSELF_FOLDED(new DrawableEllipse(Color.WHITE)), UNVISIBLE_CHILDREN_FOLDED(new DrawableEllipse(Color.GRAY)), 
	SHORTENED(new DrawableTriangle(Color.WHITE));
	final Drawable drawable;

	FoldingMark(Drawable drawable){
		this.drawable = drawable;
	}
	
	public void draw(Graphics2D g, NodeView nodeView, Point p) {
		drawable.draw(g, nodeView, p);
	}

}