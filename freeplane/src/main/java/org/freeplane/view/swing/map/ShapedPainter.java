package org.freeplane.view.swing.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.ShapeConfigurationModel;

abstract class ShapedPainter extends MainViewPainter {

	final private ShapeConfigurationModel shapeConfiguration;

	ShapedPainter(MainView mainView, ShapeConfigurationModel shapeConfiguration) {
		super(mainView);
		this.shapeConfiguration = shapeConfiguration;
	}

	@Override
	ShapeConfigurationModel getShapeConfiguration(){
		return shapeConfiguration;
	}

	@Override
    public
	Point getLeftPoint() {
		final Point in = new Point(0, mainView.getHeight() / 2);
		return in;
	}

	@Override
    public
	Point getRightPoint() {
		final Point in = getLeftPoint();
		in.x = mainView.getWidth() - 1;
		return in;
	}

	@Override
	void paintComponent(final Graphics graphics) {
		final Graphics2D g = (Graphics2D) graphics;
		final NodeView nodeView = mainView.getNodeView();
		if (nodeView.getModel() == null) {
			return;
		}
		final ModeController modeController = mainView.getNodeView().getMap().getModeController();
		final Object renderingHint = modeController.getController().getMapViewManager().setEdgesRenderingHint(g);
		mainView.paintBackgound(g);
		mainView.paintDragOver(g);
		final Color borderColor = mainView.getBorderColor();
		final Color oldColor = g.getColor();
		g.setColor(borderColor);
		final Stroke oldStroke = g.getStroke();
		g.setStroke(UITools.createStroke(mainView.getPaintedBorderWidth(), mainView.getDash().variant, BasicStroke.JOIN_MITER));
		paintNodeShape(g);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
		g.setColor(oldColor);
		g.setStroke(oldStroke);
		super.paintComponent(g);
	}

	abstract void paintNodeShape(final Graphics2D g);

}
