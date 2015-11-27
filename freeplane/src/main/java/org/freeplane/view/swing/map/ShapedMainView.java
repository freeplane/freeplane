package org.freeplane.view.swing.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;

import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.ShapeConfigurationModel;

@SuppressWarnings("serial")
abstract public class ShapedMainView extends MainView {
	
	final private ShapeConfigurationModel shapeConfiguration;

	public ShapedMainView(ShapeConfigurationModel shapeConfiguration) {
		super();
		this.shapeConfiguration = shapeConfiguration;
	}

	public ShapeConfigurationModel getShapeConfiguration(){
		return shapeConfiguration;
	}

	@Override
    public
	Point getLeftPoint() {
		final Point in = new Point(0, getHeight() / 2);
		return in;
	}

	@Override
    public
	Point getRightPoint() {
		final Point in = getLeftPoint();
		in.x = getWidth() - 1;
		return in;
	}

	public Insets getInsets(){
    	final ShapeConfigurationModel shapeConfiguration = getShapeConfiguration();
    	int horizontalMargin = shapeConfiguration.getHorizontalMargin().toBaseUnitsRounded();
    	int verticalMargin = shapeConfiguration.getVerticalMargin().toBaseUnitsRounded();
    	return new Insets(verticalMargin, horizontalMargin, verticalMargin, horizontalMargin);
    }
    
    @Override
    public Insets getInsets(Insets insets) {
        return getInsets();
    }
    
	@Override
	public Dimension getPreferredSize() {
		final Dimension preferredSize = super.getPreferredSize();
		if (isPreferredSizeSet()) {
			return preferredSize;
		}
		
		preferredSize.width = limitWidth(preferredSize.width);

		if(getShapeConfiguration().isUniform()) {
			if(preferredSize.width < preferredSize.height)
				preferredSize.width = preferredSize.height;
			else 
				preferredSize.height = preferredSize.width;
		}
		return preferredSize;
	}

	@Override
	public void paintComponent(final Graphics graphics) {
		final Graphics2D g = (Graphics2D) graphics;
		final NodeView nodeView = getNodeView();
		if (nodeView.getModel() == null) {
			return;
		}
		final ModeController modeController = getNodeView().getMap().getModeController();
		final Object renderingHint = modeController.getController().getMapViewManager().setEdgesRenderingHint(g);
		paintBackgound(g);
		paintDragOver(g);
		final Color edgeColor = nodeView.getEdgeColor();
		g.setColor(edgeColor);
		g.setStroke(MainView.DEF_STROKE);
		paintNodeShape(g);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
		super.paintComponent(g);
	}
	
	abstract protected void paintNodeShape(final Graphics2D g);

}
