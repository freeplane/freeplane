package org.freeplane.view.swing.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;

import org.freeplane.features.nodestyle.NodeStyleModel.Shape;

@SuppressWarnings("serial")
public class Hexagon2MainView extends VariableInsetsMainView {
	private static final double VERTICAL_MARGIN_FACTOR = (Math.sqrt(3) + 1)/2;
	/**
	 * 
	 */
	public Hexagon2MainView() {
        super();
    }

	@Override
	protected double getVerticalMarginFactor() {
		return VERTICAL_MARGIN_FACTOR;
	}
	
	@Override
	protected int getMinimumHorizontalInset() {
		return 3;
	}

	@Override
	protected int getMinimumVerticalInset() {
		return 3;
	}
	
	@Override
	protected double getHorizontalMarginFactor() {
		return 1.0;
	}
	
	@Override
	protected void paintNodeShape(final Graphics2D g) {
		Polygon polygon = getPaintedShape();
		g.draw(polygon);
	}

	@Override
	protected void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fill(getPaintedShape());
	}
	private static final double WIDTH_TO_HEIGHT_RELATION = Math.sqrt(3)/2;

	@Override
    public
    Shape getShape() {
		return Shape.hexagon2;
	}

	@Override
	public Dimension getPreferredSize() {
		if (isPreferredSizeSet()) {
			return super.getPreferredSize();
		}
		final Dimension prefSize = getPreferredSizeWithoutMargin(getMaximumWidth());
		int w = prefSize.width;
		int h = prefSize.height;
		int diameter = (int)(Math.ceil(Math.sqrt(w * w + h * h))) ;
		prefSize.width = (int) Math.ceil(Math.max(diameter, prefSize.width + getZoom() * getMinimumHorizontalInset()));
		prefSize.height = (int) Math.ceil(Math.max(diameter, prefSize.height + getZoom() * getMinimumVerticalInset()));
		if(prefSize.width < getMinimumWidth())
			prefSize.width = getMinimumWidth();
		if (prefSize.height < prefSize.width / WIDTH_TO_HEIGHT_RELATION)
			prefSize.height = (int) (prefSize.width / WIDTH_TO_HEIGHT_RELATION);
		else
			prefSize.width = (int) (prefSize.height * WIDTH_TO_HEIGHT_RELATION);
		return prefSize;
	}

	protected Polygon getPaintedShape() {
		int[] xCoords = new int[]{getWidth() / 2, 0,  0,  getWidth() / 2, getWidth() - 1, getWidth() - 1};
		int[] yCoords = new int[]{0,   getHeight()/4, 3 * getHeight() /4 , getHeight(),      3 * getHeight() / 4, getHeight() / 4};
		Polygon polygon = new Polygon(xCoords, yCoords, xCoords.length);
		return polygon;
	}
}
