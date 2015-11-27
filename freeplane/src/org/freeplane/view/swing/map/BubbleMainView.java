/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;

import org.freeplane.features.nodestyle.ShapeConfigurationModel;

class BubbleMainView extends ShapedMainView {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final int HORIZONTAL_MARGIN = 3;

	@Override
	protected void paintNodeShape(final Graphics2D g) {
		g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
	}

	@Override
	protected void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
	}
    
    
    public BubbleMainView(ShapeConfigurationModel shapeConfiguration) {
		super(shapeConfiguration);
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
}
