package org.freeplane.view.swing.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;

import static org.freeplane.view.swing.map.MapView.RESOURCES_SELECTED_NODE_COLOR;

interface Drawable{
	void draw(Graphics2D g, NodeView nodeView, Rectangle r);
}

class DrawableNothing implements Drawable{
	public void draw(Graphics2D g, NodeView nodeView, Rectangle r) {
	}
}
abstract class DrawableShape implements Drawable{
	static final Color standardSelectedNodeColor = ResourceController.getResourceController().getColorProperty(RESOURCES_SELECTED_NODE_COLOR);
	static final String PREFER_BORDER_COLOR_FOR_STATE_SYMBOL_BACKGROUND = "preferBorderColorForStateSymbolBackground";

	public void draw(Graphics2D g, NodeView nodeView, Rectangle r) {
		final Color color = g.getColor();
		final Color edgeColor = getEdgeColor(nodeView);
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

	protected Color getEdgeColor(NodeView nodeView) {
        final Color border = nodeView.getMainView().getBorderColor();
		final Color mapBackground = nodeView.getMap().getBackground();
		return isVisible(border, mapBackground) || isVisible(nodeView.getTextBackground(), mapBackground) ? border : nodeView.getEdgeColor();
	}

	private static boolean isVisible(Color color, Color backgroundColor) {
		return color.getAlpha() != 0 && !color.equals(backgroundColor);
	}

	protected Color getFillColor(NodeView nodeView) {
		final Color mapBackground = nodeView.getMap().getBackground();
		final Color border = nodeView.getMainView().getBorderColor();
		final Color edge = nodeView.getEdgeColor();
		if (ResourceController.getResourceController().getBooleanProperty(PREFER_BORDER_COLOR_FOR_STATE_SYMBOL_BACKGROUND)) {
			if (isVisible(border, mapBackground))
				return border;
			else if (isVisible(edge, mapBackground))
				return edge;
		}
        final Color nodeBackground = nodeView.getTextBackground();
		if (isVisible(nodeBackground, mapBackground) || isVisible(border, mapBackground) || isVisible(edge, mapBackground)) {
			return nodeBackground;
		} else {
			return standardSelectedNodeColor;
		}
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
		Color borderColor = mainView.getFoldingMarkBorderColor();
		Color fillColor = folded ? borderColor : mainView.getFoldingMarkFillColor();
		int x = r.x;
        int y = r.y;
        int width = r.width;
        int height = r.height;
        float minimumStroke = height / 8f;
        BasicStroke stroke = minimumStroke > UITools.FONT_SCALE_FACTOR * 1f ? new BasicStroke(minimumStroke) : BORDER_STROKE;
        int strokeLineWidth = (int) Math.ceil(stroke.getLineWidth() / 2);
        x += strokeLineWidth;
        y += strokeLineWidth;
        width -= strokeLineWidth*2;
        height -= strokeLineWidth*2;
        if(fillColor.getAlpha() != 255) {
			g.setColor(nodeView.getBackgroundColor());
			g.fillOval(x, y, width , height);
		}
		g.setColor(fillColor);
		g.fillOval(x, y, width , height);
		g.setColor(borderColor);
        g.setStroke(stroke);
		g.drawOval(x, y, width , height);
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
