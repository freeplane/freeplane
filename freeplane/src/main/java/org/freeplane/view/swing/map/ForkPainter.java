/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General License for more details.
 *
 *  You should have received a copy of the GNU General License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.view.swing.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Stroke;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.nodestyle.NodeGeometryModel;

class ForkPainter extends MainViewPainter {

	ForkPainter(MainView mainView) {
		super(mainView);
	}

	@Override
    Point getLeftPoint() {
		int edgeWidth = (int)mainView.getPaintedBorderWidth();
		final Point in = new Point(0, mainView.getHeight() - edgeWidth / 2);
		return in;
	}

	@Override
	int getMainViewHeightWithFoldingMark() {
		int height = mainView.getHeight();
		final NodeView nodeView = mainView.getNodeView();
		if (nodeView.isFolded()) {
			height += mainView.getZoomedFoldingSymbolHalfWidth();
		}
		return height;
	}

	@Override
    public
	Point getRightPoint() {
		int edgeWidth = (int)mainView.getPaintedBorderWidth();
		final Point in = new Point(mainView.getWidth() - 1, mainView.getHeight() - edgeWidth / 2);
		return in;
	}

	@Override
	void paintComponent(final Graphics graphics) {
		final Graphics2D g = (Graphics2D) graphics;
		final NodeView nodeView = mainView.getNodeView();
		if (nodeView.getModel() == null) {
			return;
		}
		mainView.paintBackgound(g);
		mainView.paintDragOver(g);
		super.paintComponent(g);
	}

	@Override
	void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fillRect(0, 0, mainView.getWidth(), mainView.getHeight() - (int)mainView.getPaintedBorderWidth());
	}

	@Override
	void paintDecoration(final NodeView nodeView, final Graphics2D g) {
		final Stroke oldStroke = g.getStroke();
		g.setStroke(UITools.createStroke(mainView.getPaintedBorderWidth(), mainView.getDash().variant, BasicStroke.JOIN_MITER));
		final Color oldColor = g.getColor();
		g.setColor(mainView.getBorderColor());
		Point leftLinePoint = getLeftPoint();
		g.drawLine(leftLinePoint.x, leftLinePoint.y, leftLinePoint.x + mainView.getWidth(), leftLinePoint.y);
		g.setColor(oldColor);
		g.setStroke(oldStroke);
		super.paintDecoration(nodeView, g);
    }

    @Override
    Insets getInsets() {
        return  getInsets(null);
    }

	@Override
    Insets getInsets(Insets insets) {
    	final NodeView nodeView = mainView.getNodeView();
        int edgeWidth = nodeView.getEdgeWidth();
        edgeWidth = Math.round(mainView.getUnzoomedBorderWidth());
		if(insets == null)
    		insets = new Insets(0, 2, edgeWidth, 2);
    	else
    		insets.set(0, 2, edgeWidth, 2);
        return insets;
    }
	private final static int SINGLE_CHILD_SHIFT = -2;

	@Override
	int getSingleChildShift() {
		return SINGLE_CHILD_SHIFT;
	}

	@Override
	NodeGeometryModel getShapeConfiguration() {
		return NodeGeometryModel.FORK;
	}

}
