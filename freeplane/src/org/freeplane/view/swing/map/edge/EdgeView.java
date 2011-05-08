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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

/**
 * This class represents a single Edge of a MindMap.
 */
public abstract class EdgeView {
	protected static final BasicStroke DEF_STROKE = new BasicStroke();
	static Stroke ECLIPSED_STROKE = null;

	protected static Stroke getEclipsedStroke() {
		if (EdgeView.ECLIPSED_STROKE == null) {
			final float dash[] = { 3.0f, 9.0f };
			EdgeView.ECLIPSED_STROKE = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 12.0f, dash,
			    0.0f);
		}
		return EdgeView.ECLIPSED_STROKE;
	}

	private final NodeView source;
	protected Point start, end;
	
	public void setStart(Point start) {
    	this.start = start;
    }

	public Point getStart() {
    	return start;
    }

	public void setEnd(Point end) {
    	this.end = end;
    }

	public Point getEnd() {
    	return end;
    }

	private final NodeView target;
	private Color color;
	private Integer width;

	protected void createStart() {
        final MainView mainView = source.getMainView();
        final MainView targetMainView = target.getMainView();
        
        final Point relativeLocation = source.getRelativeLocation(target);
        relativeLocation.x += targetMainView.getWidth()/2;
        relativeLocation.y += targetMainView.getHeight()/2;
        start = mainView.getConnectorPoint(relativeLocation);
                
        relativeLocation.x -= targetMainView.getWidth()/2;
        relativeLocation.y -= targetMainView.getHeight()/2;
        relativeLocation.x = - relativeLocation.x + mainView.getWidth()/2;
        relativeLocation.y = - relativeLocation.y + mainView.getHeight()/2;
		end = target.getMainView().getConnectorPoint(relativeLocation);
	}

	protected void align(Point start, Point end) {
		if(1 == Math.abs(start.y - end.y)){
			end.y = start.y; 
		}
    }

	public Color getColor() {
		if (color == null) {
			color = target.getEdgeColor();
		}
		return color;
	}

	public void setColor(final Color color) {
		this.color = color;
	}

	protected MapView getMap() {
		return getTarget().getMap();
	}

	/**
	 * @return Returns the source.
	 */
	public NodeView getSource() {
		return source;
	}

	protected Stroke getStroke() {
		final int width = getWidth();
		if (width < 0) {
			return EdgeView.DEF_STROKE;
		}
		if (width == 0) {
			return EdgeView.DEF_STROKE;
		}
		return new BasicStroke(width * getMap().getZoom(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	}

	/**
	 * @return Returns the target.
	 */
	public NodeView getTarget() {
		return target;
	}

	public int getWidth() {
		if (width != null) {
			return width;
		}
		final int width = target.getEdgeWidth();
		return width;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	protected boolean isTargetEclipsed() {
		return getTarget().isParentHidden();
	}

	abstract protected void draw(Graphics2D g);

	public EdgeView(final NodeView target) {
		source = target.getVisibleParentView();
		this.target = target;
		createStart();
        UITools.convertPointToAncestor(target.getMainView(), end, source);
        UITools.convertPointToAncestor(source.getMainView(), start, source);
        align(start, end);
	}

	public void paint(final Graphics2D g) {
		final Stroke stroke = g.getStroke();
		draw(g);
		g.setStroke(stroke);
	}

	public EdgeView(final NodeView source, final NodeView target) {
		this.source = source;
		this.target = target;
		createStart();
        final MapView map = getMap();
        UITools.convertPointToAncestor(target.getMainView(), end, map);
		UITools.convertPointToAncestor(source.getMainView(), start, map);
        align(start, end);
	}

	abstract public boolean detectCollision(Point p);
}
