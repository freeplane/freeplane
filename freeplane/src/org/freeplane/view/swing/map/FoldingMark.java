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
		int shapeWidth = getWidth(nodeView);
		p.translate(-shapeWidth/2, -shapeWidth/2);
		final Shape shape = getShape(p.x, p.y, shapeWidth);
		g.setColor(getFillColor(nodeView, p));
		g.fill(shape);
		g.setColor(edgeColor);
		drawShape(g, shape, p, nodeView);
		g.setColor(color);
	}
	protected void drawShape(Graphics2D g, final Shape shape, Point p, NodeView nodeView) {
		g.draw(shape);
	}
	protected int getWidth(NodeView nodeView) {
		int zoomedFoldingSymbolHalfWidth = nodeView.getMainView().getZoomedFoldingSymbolHalfWidth();
		int shapeWidth = zoomedFoldingSymbolHalfWidth * 2;
		return shapeWidth;
	}
	
	abstract Shape getShape(int x, int y, int width);
	protected Color getFillColor(NodeView nodeView, Point p) {
		return fillColor;
	}
}

class DrawableEllipse extends DrawableShape{
	public DrawableEllipse(Color fillColor) {
		super(fillColor);
	}
	Shape getShape(int x, int y, int width){
		return new Ellipse2D.Float(x, y, width, width);
	}
}

class FoldingCircle extends DrawableEllipse{

	private static final int WIDTH = 16;
	public FoldingCircle(Color fillColor) {
		super(fillColor);
	}

	@Override
	protected void drawShape(Graphics2D g, Shape shape, Point p, NodeView nodeView) {
		super.drawShape(g, shape, p, nodeView);
		if(nodeView.getMainView().getMouseArea().equals(MouseArea.FOLDING))
			g.setColor(Color.WHITE);
		g.drawLine(p.x + WIDTH / 4, p.y + WIDTH / 2, p.x + WIDTH * 3/ 4, p.y + WIDTH / 2);
		if(nodeView.getModel().isFolded())
			g.drawLine(p.x + WIDTH / 2, p.y + WIDTH / 4, p.x + WIDTH / 2, p.y + WIDTH * 3 / 4);
	}

	@Override
	protected int getWidth(NodeView nodeView) {
		return WIDTH;
	}

	@Override
	protected Color getFillColor(NodeView nodeView, Point p) {
		if(nodeView.getMainView().getMouseArea().equals(MouseArea.FOLDING)){
			return Color.GRAY;
		}
		return super.getFillColor(nodeView, p);
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
	SHORTENED(new DrawableTriangle(Color.WHITE)), FOLDING_CIRCLE(new FoldingCircle(Color.WHITE));
	final Drawable drawable;

	FoldingMark(Drawable drawable){
		this.drawable = drawable;
	}
	
	public void draw(Graphics2D g, NodeView nodeView, Point p) {
		drawable.draw(g, nodeView, p);
	}

}