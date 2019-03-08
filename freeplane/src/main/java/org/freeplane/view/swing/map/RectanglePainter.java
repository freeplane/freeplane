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

import org.freeplane.features.nodestyle.ShapeConfigurationModel;

class RectanglePainter extends ShapedPainter {

    RectanglePainter(MainView mainView, ShapeConfigurationModel shapeConfiguration) {
		super(mainView, shapeConfiguration);
	}

	@Override
	void paintNodeShape(final Graphics2D g) {
		final int zoomedEdgeWidth = (int) mainView.getPaintedBorderWidth();
		g.drawRect(zoomedEdgeWidth / 2, zoomedEdgeWidth / 2, mainView.getWidth() - zoomedEdgeWidth, mainView.getHeight() - zoomedEdgeWidth);
	}

	@Override
	void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fillRect(0, 0, mainView.getWidth() - 1, mainView.getHeight() - 1);
	}


	@Override
	Insets getInsets(){
		int edgeWidthInset = (int) (mainView.getUnzoomedBorderWidth() - 1);
    	final ShapeConfigurationModel shapeConfiguration = getShapeConfiguration();
    	int horizontalMargin = shapeConfiguration.getHorizontalMargin().toBaseUnitsRounded() + edgeWidthInset;
    	int verticalMargin = shapeConfiguration.getVerticalMargin().toBaseUnitsRounded() + edgeWidthInset;
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

		preferredSize.width = mainView.limitWidth(preferredSize.width);

		if(getShapeConfiguration().isUniform()) {
			if(preferredSize.width < preferredSize.height)
				preferredSize.width = preferredSize.height;
			else
				preferredSize.height = preferredSize.width;
		}
		return preferredSize;
	}


}
