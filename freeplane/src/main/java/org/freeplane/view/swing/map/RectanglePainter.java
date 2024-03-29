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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;

import org.freeplane.api.Dash;
import org.freeplane.features.nodestyle.NodeGeometryModel;

class RectanglePainter extends ShapedPainter {

    RectanglePainter(MainView mainView, NodeGeometryModel shapeConfiguration) {
		super(mainView, shapeConfiguration);
	}

	@Override
	void paintNodeShape(final Graphics2D g) {
		final int zoomedEdgeWidth = mainView.getPaintedBorderWidth();
		g.drawRect(zoomedEdgeWidth / 2, zoomedEdgeWidth / 2, mainView.getWidth() - zoomedEdgeWidth, mainView.getHeight() - zoomedEdgeWidth);
	}

	@Override
	void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		final int borderWidth = mainView.getPaintedBorderWidth();
		final int overlapWidth = mainView.getDash() == Dash.SOLID  ? 1 : 0;
		graphics.fillRect(borderWidth, borderWidth, mainView.getWidth() - 2 * borderWidth + overlapWidth, mainView.getHeight() - 2 * borderWidth + overlapWidth);
	}


	@Override
	Insets getInsets(){
		int borderWidthInset = (int) (mainView.getUnzoomedBorderWidth() - 1);
    	final NodeGeometryModel shapeConfiguration = getShapeConfiguration();
    	int horizontalMargin = shapeConfiguration.getHorizontalMargin().toBaseUnitsRounded() + borderWidthInset;
    	int verticalMargin = shapeConfiguration.getVerticalMargin().toBaseUnitsRounded() + borderWidthInset;
    	return new Insets(verticalMargin, horizontalMargin, verticalMargin, horizontalMargin);
    }

    @Override
    Insets getInsets(Insets insets) {
        return getInsets();
    }

	@Override
	Dimension getPreferredSize() {
		final Dimension preferredSize = super.getPreferredSize();
		if (mainView.isPreferredSizeSet()) {
			return preferredSize;
		}

		preferredSize.width = mainView.limitWidth(preferredSize.width, mainView.getPaintedBorderWidth());

		if(getShapeConfiguration().isUniform()) {
			if(preferredSize.width < preferredSize.height)
				preferredSize.width = preferredSize.height;
			else
				preferredSize.height = preferredSize.width;
		}
		return preferredSize;
	}


}
