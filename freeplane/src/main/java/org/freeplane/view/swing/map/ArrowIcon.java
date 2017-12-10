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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

import org.freeplane.features.mode.ModeController;

/**
 * @author Dimitry Polivaev
 * Mar 5, 2011
 */
class ArrowIcon implements Icon{
	/**
     * 
     */
    private final NodeView nodeView;
	final private boolean down;
	final private static int ARROW_HEIGTH = 5;
	final private static int ARROW_HALF_WIDTH = 4;
	final private static int ICON_HEIGTH = ARROW_HEIGTH + 2;
	final private static int ICON_WIDTH = 1 + ARROW_HALF_WIDTH * 2 + 1;
	

	public ArrowIcon(NodeView nodeView, boolean down) {
        super();
		this.nodeView = nodeView;
        this.down = down;
    }

	public int getIconHeight() {
		return ICON_HEIGTH; 
    }

	public int getIconWidth() {
		return ICON_WIDTH; 
    }

	public void paintIcon(Component c, Graphics g, int x, int y) {
		int[]   xs = new int[3];
		int[]   ys = new int[3];
		
		xs[0] = 1 + ARROW_HALF_WIDTH;
		xs[1] = 1;
		xs[2] = xs[0] + ARROW_HALF_WIDTH;
		if(down){
			ys[0] = 1 + ARROW_HEIGTH;
			ys[1] = ys[2] = 1;
		}
		else{
			ys[0] = 1;
			ys[1] = ys[2] = 1 + ARROW_HEIGTH;
		}
		Graphics2D g2= (Graphics2D) g;
		final Object renderingHint = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		ModeController modeController = this.nodeView.getMap().getModeController();
		modeController.getController().getMapViewManager().setEdgesRenderingHint(g2);
		g.drawPolygon(xs, ys, 3); 
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
    }
	
}