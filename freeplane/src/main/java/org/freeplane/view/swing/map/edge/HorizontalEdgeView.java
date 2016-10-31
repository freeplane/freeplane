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
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Line2D;

import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.link.CollisionDetector;

/**
 * This class represents a single Edge of a MindMap.
 */
public class HorizontalEdgeView extends EdgeView {
	private int xs[];
	private int ys[];

	public HorizontalEdgeView(NodeView source, NodeView target, Component paintedComponent) {
	    super(source, target, paintedComponent);
    }

	@Override
	protected void createStart() {
	    if(getSource().isRoot() && ! MainView.USE_COMMON_OUT_POINT_FOR_ROOT_NODE){
	        super.createStart();
	    }
	    else{
	        if(getTarget().isLeft()){
	            start = getSource().getMainView().getLeftPoint();
	        }
	        else{
	            start = getSource().getMainView().getRightPoint();
	        }
	    }
        if(getTarget().isLeft()){
            end = getTarget().getMainView().getRightPoint();
        }
        else{
            end = getTarget().getMainView().getLeftPoint();
        }
    }

    @Override
	protected void draw(final Graphics2D g) {
		final Color color = getColor();
		g.setColor(color);
		final Stroke stroke = getStroke();
		g.setStroke(stroke);
		int xMiddle = getTarget().getMap().getZoomed(LocationModel.DEFAULT_HGAP_PX) / 2;
		final boolean left = getTarget().isLeft() 
		    || ! MainView.USE_COMMON_OUT_POINT_FOR_ROOT_NODE && getSource().isRoot()&& start.x > end.x;
        if (left) {
			xMiddle = -xMiddle;
		}
		xMiddle += start.x;
		xs = new int[] { start.x, xMiddle, xMiddle, end.x };
		ys = new int[] { start.y, start.y, end.y, end.y };
		g.drawPolyline(xs, ys, 4);
		if (isTargetEclipsed()) {
			g.setColor(g.getBackground());
			g.setStroke(EdgeView.getEclipsedStroke());
			g.drawPolyline(xs, ys, 4);
			g.setColor(color);
			g.setStroke(stroke);
		}
	}

	@Override
	public boolean detectCollision(final Point p) {
		final CollisionDetector collisionDetector = new CollisionDetector();
		for (int i = 1; i < xs.length; i++) {
			if (collisionDetector.detectCollision(p, new Line2D.Float(xs[i - 1], ys[i - 1], xs[i], ys[i]))) {
				return true;
			}
		}
		return false;
	}
}
