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
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.DashVariant;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MainView.ConnectorLocation;
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
    private ConnectorLocation startConnectorLocation;
    private ConnectorLocation endConnectorLocation;
	private int[] dash;

	protected void createStart() {
        final MainView mainView = source.getMainView();
        final MainView targetMainView = target.getMainView();
        
        final Point relativeLocation = source.getRelativeLocation(target);
        relativeLocation.x += targetMainView.getWidth()/2;
        relativeLocation.y += targetMainView.getHeight()/2;
        start = mainView.getConnectorPoint(relativeLocation);
        startConnectorLocation = mainView.getConnectorLocation(relativeLocation);
                
        relativeLocation.x -= targetMainView.getWidth()/2;
        relativeLocation.y -= targetMainView.getHeight()/2;
        relativeLocation.x = - relativeLocation.x + mainView.getWidth()/2;
        relativeLocation.y = - relativeLocation.y + mainView.getHeight()/2;
		end = target.getMainView().getConnectorPoint(relativeLocation);
		endConnectorLocation = targetMainView.getConnectorLocation(relativeLocation);
	}

	protected ConnectorLocation getStartConnectorLocation() {
        return startConnectorLocation;
    }

    protected ConnectorLocation getEndConnectorLocation() {
        return endConnectorLocation;
    }

    protected Point getControlPoint(ConnectorLocation startConnectorLocation){
        final int xctrl; 
        final int yctrl; 
        if(ConnectorLocation.LEFT.equals(startConnectorLocation)){
            xctrl= - 1;
            yctrl = 0;
        }
        else if(ConnectorLocation.RIGHT.equals(startConnectorLocation)){
            xctrl= 1;
            yctrl = 0;
        }
        else if(ConnectorLocation.TOP.equals(startConnectorLocation)){
            xctrl= 0;
            yctrl = - 1;
        }
        else if(ConnectorLocation.LEFT.equals(startConnectorLocation)){
            xctrl= 0;
            yctrl = 1;
        }
        else {
            xctrl = 0;
            yctrl = 0;
        }
        return new Point(xctrl, yctrl);
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
		return getStroke(width);
	}

	protected Stroke getStroke(final float width) {
		int[] dash = getDash();
		if (width <= 0 && dash == null) {
			return EdgeView.DEF_STROKE;
		}
		final int[] dash1 = dash;
    	return UITools.createStroke(width * getMap().getZoom(), dash1, BasicStroke.JOIN_ROUND);
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

	public int[] getDash() {
		if (dash != null) {
			return dash;
		}
		final DashVariant dash = target.getEdgeDash();
		return dash.variant;
	}

	public void setDash(final int[] dash) {
		this.dash = dash;
	}

	protected boolean drawHiddenParentEdge() {
		return getTarget().isParentHidden();
	}

	abstract protected void draw(Graphics2D g);

	public void paint(final Graphics2D g) {
		final Stroke stroke = g.getStroke();
		final Color color = g.getColor();
		draw(g);
		g.setStroke(stroke);
		g.setColor(color);
	}

	public EdgeView(final NodeView source, final NodeView target, final Component paintedComponent) {
		this.source = source;
		this.target = target;
		createStart();
        UITools.convertPointToAncestor(target.getMainView(), end, paintedComponent);
		UITools.convertPointToAncestor(source.getMainView(), start, paintedComponent);
        align(start, end);
	}

	abstract public boolean detectCollision(Point p);
}
