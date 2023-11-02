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
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Stroke;

import org.freeplane.api.Dash;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.nodestyle.NodeGeometryModel;

class ForkPainter extends ShapedPainter {

	ForkPainter(MainView mainView) {
		super(mainView, NodeGeometryModel.FORK);
	}

	@Override
	public
    Point getLeftPoint() {
		int edgeWidth = mainView.getPaintedBorderWidth();
		final Point in = new Point(0, mainView.getHeight() - edgeWidth / 2);
		return in;
	}

	@Override
	int getMainViewHeightWithFoldingMark(boolean onlyFolded) {
	    final NodeView nodeView = mainView.getNodeView();
	    if(nodeView.usesHorizontalLayout())
	        return super.getMainViewHeightWithFoldingMark(onlyFolded);
		int height = mainView.getHeight();
		if (! onlyFolded || nodeView.isFolded()) {
			height += mainView.getZoomedFoldingMarkHalfWidth();
		}
		return height;
	}

	@Override
    public
	Point getRightPoint() {
		int borderWidth = mainView.getPaintedBorderWidth();
		final Point in = new Point(mainView.getWidth() - 1, mainView.getHeight() - borderWidth / 2);
		return in;
	}

	@Override
	void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(Color.DARK_GRAY);
		graphics.fillRect(0, 0, mainView.getWidth(), mainView.getHeight() - mainView.getPaintedBorderWidth() +  (mainView.getDash() == Dash.SOLID  ? 1 : 0));
	}

	@Override
	void paintNodeShape(final Graphics2D g) {
		final Stroke oldStroke = g.getStroke();
		g.setStroke(UITools.createStroke(mainView.getPaintedBorderWidth(), mainView.getDash().pattern, BasicStroke.JOIN_MITER));
		final Color oldColor = g.getColor();
		g.setColor(mainView.getBorderColor());
		Point leftLinePoint = getLeftPoint();
		g.drawLine(leftLinePoint.x, leftLinePoint.y, leftLinePoint.x + mainView.getWidth(), leftLinePoint.y);
		g.setColor(oldColor);
		g.setStroke(oldStroke);
    }

    @Override
    Insets getInsets() {
        return  getInsets(null);
    }

	@Override
    Insets getInsets(Insets insets) {
        int borderWidth =  Math.round(mainView.getUnzoomedBorderWidth());
		if(insets == null)
    		insets = new Insets(0, 2, borderWidth, 2);
    	else
    		insets.set(0, 2, borderWidth, 2);
        return insets;
    }
	private final static int SINGLE_CHILD_SHIFT = -2;

	@Override
	int getSingleChildShift() {
		return SINGLE_CHILD_SHIFT;
	}
}
