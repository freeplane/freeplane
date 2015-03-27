/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
import java.awt.Graphics;

/**
 * @author Dimitry Polivaev
 * Mar 5, 2011
 */
@SuppressWarnings("serial")
public
final class DetailsView extends ZoomableLabel {
    public DetailsView() {
        super();
   }

    @Override
    protected void paintComponent(Graphics g) {
    	if(isBackgroundSet()){
    		final Color background = getBackground();
    		final Color oldColor = g.getColor();
    		g.setColor(background);
    		final int iconWidth = getIconWidth() + getIconTextGap();
    		g.fillRect(iconWidth, 0, getWidth() - iconWidth, getHeight());
    		g.setColor(oldColor);
    	}
        super.paintComponent(g);
    }
	

}