package org.freeplane.view.swing.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.freeplane.core.ui.components.UITools;

interface Drawable{
	void draw(Graphics2D g, NodeView nodeView, Rectangle r);
}

class DrawableNothing implements Drawable{
	public void draw(Graphics2D g, NodeView nodeView, Rectangle r) {
	}
}
abstract class DrawableShape implements Drawable{

	public void draw(Graphics2D g, NodeView nodeView, Rectangle r) {
		final Color color = g.getColor();
		final Color edgeColor = nodeView.getMainView().getBorderColor();
		final Shape shape = getShape(r);
		Color fillColor = getFillColor(nodeView);
		if(fillColor != null) {
			g.setColor(fillColor);
			g.fill(shape);
		}
		g.setColor(edgeColor);
		drawShape(g, shape, r, nodeView);
		g.setColor(color);
	}
	protected void drawShape(Graphics2D g, final Shape shape, Rectangle r, NodeView nodeView) {
		g.draw(shape);
	}
	abstract Shape getShape(Rectangle r);
	protected Color getFillColor(NodeView nodeView) {
		return nodeView.getTextBackground();
	}
}

class DrawableEllipse extends DrawableShape{
	public DrawableEllipse() {
		super();
	}
	Shape getShape(Rectangle r){
		return new Ellipse2D.Float(r.x, r.y, r.width, r.height);
	}
}

class FoldingCircle implements Drawable{
	private static final BasicStroke BORDER_STROKE = new BasicStroke(UITools.FONT_SCALE_FACTOR * 1f);
	final private boolean folded;

	public FoldingCircle(boolean folded) {
		super();
		this.folded  = folded;
	}

	@Override
	public void draw(Graphics2D g, NodeView nodeView, Rectangle r) {
		final MainView mainView = nodeView.getMainView();
		Color borderColor = mainView.getFoldingCircleBorderColor();
		Color fillColor = folded ? borderColor : mainView.getFoldingCircleFillColor();
		g.setColor(fillColor);
		g.fillOval(r.x, r.y, r.width , r.height);
		g.setColor(borderColor);
		g.setStroke(BORDER_STROKE);
		g.drawOval(r.x, r.y, r.width , r.height);
	}

}

class DrawableTriangle extends DrawableShape{
	public DrawableTriangle() {
		super();
	}
	Shape getShape(Rectangle r){
		final Polygon polygon = new Polygon();
		polygon.addPoint(r.x, r.y);
		polygon.addPoint(r.x + r.width, r.y);
		polygon.addPoint(r.x + (r.width + 1) / 2, r.y + r.height);
		polygon.addPoint(r.x + r.width / 2, r.y + r.height);
		polygon.addPoint(r.x, r.y);
		return polygon;
	}
}


class DrawableRectangle extends DrawableShape{
	public DrawableRectangle() {
		super();
	}
	Shape getShape(Rectangle r){
		final Polygon polygon = new Polygon();
		final int x1 = r.x + 1;
		final int x2 = x1 + r.width - 3;
		final int y1 = r.y + 1;
		final int y2 = y1 + r.height - 3;
		polygon.addPoint(x1, y1);
		polygon.addPoint(x2, y1);
		polygon.addPoint(x2, y2);
		polygon.addPoint(x1, y2);
		polygon.addPoint(x1, y1);
		return polygon;
	}
}
public enum FoldingMark implements Drawable{
	INVISIBLE(new DrawableNothing()),
	SHORTENED(new DrawableTriangle()),
	CLONE(new DrawableRectangle()),
	FOLDING_CIRCLE_FOLDED(new FoldingCircle(true)),
	FOLDING_CIRCLE_UNFOLDED(new FoldingCircle(false));
	final Drawable drawable;

	FoldingMark(Drawable drawable){
		this.drawable = drawable;
	}

	public void draw(Graphics2D g, NodeView nodeView, Rectangle r) {
		drawable.draw(g, nodeView, r);
	}

}