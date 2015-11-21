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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.SwingConstants;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.nodelocation.LocationModel;

abstract class OvalMainView extends ShapedMainView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public OvalMainView() {
        super();
        setHorizontalAlignment(SwingConstants.CENTER);
    }

	@Override
	protected void paintNodeShape(final Graphics2D g) {
		g.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
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
    	final int margin = new Quantity<LengthUnits>(getFont().getSize2D() * insetsScalingFactor(), LengthUnits.pt).toBaseUnitsRounded();
    	insets.left=insets.right=insets.top=insets.bottom=margin;
        return insets;
    }

	abstract protected double insetsScalingFactor();

	protected Point getConnectorPointAtTheOvalBorder(Point p) {
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
