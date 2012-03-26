package org.freeplane.view.swing.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

enum FoldingMark {
	UNFOLDED(Color.WHITE), ITSELF_FOLDED(Color.WHITE), UNVISIBLE_CHILDREN_FOLDED(Color.WHITE), SHORTENED(Color.GRAY);
	final Color fillColor;

	FoldingMark(Color fillColor){
		this.fillColor = fillColor;
	}
	
	void draw(NodeView nodeView, Graphics2D g, Point p) {
		final Color color = g.getColor(); 
		final Color edgeColor = nodeView.getEdgeColor();
		int zoomedFoldingSymbolHalfWidth = nodeView.getMainView().getZoomedFoldingSymbolHalfWidth();
		p.translate(-zoomedFoldingSymbolHalfWidth, -zoomedFoldingSymbolHalfWidth);
		int shapeWidth = zoomedFoldingSymbolHalfWidth * 2;
		final Shape shape = getShape(p.x, p.y, shapeWidth);
		g.setColor(fillColor);
		g.fill(shape);
		g.setColor(edgeColor);
		g.draw(shape);
		g.setColor(color);
	}

	private Shape getShape(int x, int y, int width){
		if(equals(SHORTENED)){
			final Polygon polygon = new Polygon();
			polygon.addPoint(x, y);
			polygon.addPoint(x + width, y);
			polygon.addPoint(x + width / 2, y + width*2/3);
			polygon.addPoint(x, y);
			return polygon;
		}
		else{
			return new Ellipse2D.Float(x, y, width, width);
		}
	}

}