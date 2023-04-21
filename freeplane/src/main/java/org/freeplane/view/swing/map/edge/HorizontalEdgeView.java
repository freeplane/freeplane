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

import org.freeplane.api.ChildNodesAlignment;
import org.freeplane.api.ChildrenSides;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MainView.ConnectorLocation;
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
	    NodeView source = getSource();
	    NodeView target = getTarget();
	    boolean usesHorizontalLayout = source.usesHorizontalLayout();
	    ChildNodesAlignment childNodesAlignment = source.getChildNodesAlignment();
	    if(! usesHorizontalLayout
                && ! (source.isRoot() && MainView.USE_COMMON_OUT_POINT_FOR_ROOT_NODE)
                && childNodesAlignment.isStacked()
                && source.childrenSides() != ChildrenSides.BOTH_SIDES) {
	        super.createStart();
	        if(getStartConnectorLocation() == ConnectorLocation.RIGHT)
	            start.x -= getWidth() / 2;
	        else if(getStartConnectorLocation() == ConnectorLocation.LEFT)
                start.x += getWidth() / 2;
	        return;
	    }


        if(source.isRoot() && ! MainView.USE_COMMON_OUT_POINT_FOR_ROOT_NODE){
	        super.createStart();
	    }
	    else{
	        MainView mainView = source.getMainView();

	        if(usesHorizontalLayout) {
	            if(source.getChildNodesAlignment() == ChildNodesAlignment.AFTER_PARENT)
                    start = mainView.getRightPoint();
                else if(source.getChildNodesAlignment() == ChildNodesAlignment.BEFORE_PARENT)
                    start = mainView.getLeftPoint();
                else if(target.isTopOrLeft()){
	                start = mainView.getTopPoint();
	            }
	            else{
	                start = mainView.getBottomPoint();
	            }

	        }
	        else {
	            if(source.getChildNodesAlignment() == ChildNodesAlignment.AFTER_PARENT)
	                start = mainView.getBottomPoint();
	            else if(source.getChildNodesAlignment() == ChildNodesAlignment.BEFORE_PARENT)
	                start = mainView.getTopPoint();
	            else if(target.isTopOrLeft()){
	                start = mainView.getLeftPoint();
	            }
	            else{
	                start = mainView.getRightPoint();
	            }

	        }

	    }
        MainView mainView = target.getMainView();
        if(target.isTopOrLeft()){
            end = usesHorizontalLayout ? mainView.getBottomPoint() : mainView.getRightPoint();
        }
        else{
            end = usesHorizontalLayout ? mainView.getTopPoint() : mainView.getLeftPoint();
        }
    }

    @Override
	protected void draw(final Graphics2D g) {
		final Color color = getColor();
		g.setColor(color);
		final Stroke stroke = getStroke();
		g.setStroke(stroke);
		NodeView source = getSource();
        boolean usesHorizontalLayout = source.usesHorizontalLayout();
        boolean areChildrenApart = source.getChildNodesAlignment().isStacked();
        if(usesHorizontalLayout) {
            int middleY = (start.y + end.y) / 2;
            xs = new int[] { start.x, start.x, end.x, end.x };
            ys = new int[] { start.y, middleY, middleY, end.y };
        }
        else if(areChildrenApart) {
            xs = new int[] { start.x, start.x, end.x };
            ys = new int[] { start.y, end.y, end.y };
        }
        else {
		    int middleX = (start.x + end.x) / 2;
		    xs = new int[] { start.x, middleX, middleX, end.x };
		    ys = new int[] { start.y, start.y, end.y, end.y };
		}
		g.drawPolyline(xs, ys, xs.length);
		if (drawHiddenParentEdge()) {
		    g.setColor(g.getBackground());
			g.setStroke(EdgeView.getEclipsedStroke());
			g.drawPolyline(xs, ys, xs.length);
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
