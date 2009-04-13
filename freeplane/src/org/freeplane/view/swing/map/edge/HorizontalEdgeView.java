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
package org.freeplane.view.swing.map.edge;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import org.freeplane.features.common.nodelocation.LocationModel;

/**
 * This class represents a single Edge of a MindMap.
 */
public class HorizontalEdgeView extends EdgeView {
	public HorizontalEdgeView() {
		super();
	}

	@Override
	protected void paint(final Graphics2D g) {
		final Color color = getColor();
		g.setColor(color);
		final Stroke stroke = getStroke();
		g.setStroke(stroke);
		final int w = getWidth();
		int xMiddle = getTarget().getMap().getZoomed(LocationModel.HGAP) / 2;
		if(getTarget().isLeft()){
			xMiddle = - xMiddle;
		}
		xMiddle += start.x;
		int endX = end.x;
		int mainViewWidth = getTarget().getMainView().getWidth();
		if(getTarget().isLeft()){
			if(end.x - mainViewWidth/2> xMiddle){
				endX -= mainViewWidth; 
			}
		}
		else{
			if(end.x + mainViewWidth/2< xMiddle){
				endX += mainViewWidth; 
			}
		}
		int xs[] = { start.x, xMiddle, xMiddle,  endX };
		final int ys[] = { start.y, start.y, end.y, end.y };
		if (w <= 1) {
			g.drawPolyline(xs, ys, 4);
			if (isTargetEclipsed()) {
				g.setColor(g.getBackground());
				g.setStroke(EdgeView.getEclipsedStroke());
				g.drawPolyline(xs, ys, 4);
				g.setColor(color);
				g.setStroke(stroke);
			}
		}
		else {
			int dx = w / 3 + 1;
			if (getTarget().isLeft()) {
				dx = -dx;
			}
			
			
			g.drawPolyline(xs, ys, 4);
			if (isTargetEclipsed()) {
				g.setColor(g.getBackground());
				g.setStroke(EdgeView.getEclipsedStroke());
				g.drawPolyline(xs, ys, 4);
				g.setColor(color);
				g.setStroke(stroke);
			}
		}
	}
}
