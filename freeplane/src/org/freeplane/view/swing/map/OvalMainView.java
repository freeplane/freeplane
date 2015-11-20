/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.view.swing.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.SwingConstants;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.NodeStyleModel.Shape;
import org.freeplane.features.nodestyle.NodeStyleModel.TextAlign;

abstract class OvalMainView extends MainView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public OvalMainView() {
        super();
        setHorizontalAlignment(SwingConstants.CENTER);
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


	@Override
	public void paintComponent(final Graphics graphics) {
		final Graphics2D g = (Graphics2D) graphics;
		if (getNodeView().getModel() == null) {
			return;
		}
		final ModeController modeController = getNodeView().getMap().getModeController();
		final Object renderingHint = modeController.getController().getMapViewManager().setEdgesRenderingHint(g);
		paintBackgound(g);
		paintDragOver(g);
		g.setColor(Color.gray);
		g.setStroke(new BasicStroke(1.0f));
		g.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
		super.paintComponent(g);
	}

	@Override
	protected void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fillOval(1, 1, getWidth() - 2, getHeight() - 2);
	}

    @Override
    public Insets getInsets() {
        return getInsets(new Insets(0, 0, 0, 0));
    }

    @Override
    public Insets getInsets(Insets insets) {
    	if(insets == null)
    		return getInsets();
    	final int margin = new Quantity<LengthUnits>(getFont().getSize2D() * 0.2, LengthUnits.pt).toBaseUnitsRounded();
    	insets.left=insets.right=insets.top=insets.bottom=margin;
        return insets;
    }

    @Override
    public Point getConnectorPoint(Point p) {
    	if (USE_COMMON_OUT_POINT_FOR_ROOT_NODE ||  ! getNodeView().isRoot()) {
    		return super.getConnectorPoint(p);
    	}
    	final double nWidth = this.getWidth() / 2f;
    	final double nHeight = this.getHeight() / 2f;
    	int dx = Math.max(Math.abs(p.x -  this.getWidth()/2), getNodeView().getZoomed(LocationModel.HGAP));
    	if(p.x < this.getWidth()/2)
    		dx = -dx;
    	double angle = Math.atan((p.y - nHeight) / dx);
    	if (dx < 0) {
    		angle += Math.PI;
    	}
    	final Point out = new Point((int) ((1f + Math.cos(angle)) * nWidth), (int) ((1f + Math.sin(angle)) * nHeight));
    	return out;
    }
}
