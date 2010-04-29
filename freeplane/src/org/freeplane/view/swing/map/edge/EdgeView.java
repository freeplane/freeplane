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

import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.edge.EdgeModel;
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
	public Point getStart() {
    	return start;
    }

	public Point getEnd() {
    	return end;
    }

	private final NodeView target;
	private Color color;
	private Integer width;

	protected void createStart() {
		start = source.getMainViewOutPoint(getTarget(), end);
		UITools.convertPointToAncestor(source.getMainView(), start, source);
	}

	public Color getColor() {
		if (color == null) {
			final NodeModel model = target.getModel();
			color = EdgeController.getController(target.getMap().getModeController()).getColor(model);
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
		if (width == EdgeModel.WIDTH_THIN) {
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
		final NodeModel model = target.getModel();
		final int width = EdgeController.getController(target.getMap().getModeController()).getWidth(model);
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
		end = getTarget().getMainViewInPoint();
		UITools.convertPointToAncestor(target.getMainView(), end, source);
		createStart();
	}

	public void paint(final Graphics2D g) {
		final Stroke stroke = g.getStroke();
		draw(g);
		g.setStroke(stroke);
	}

	public EdgeView(final NodeView source, final NodeView target) {
		this.source = source;
		this.target = target;
		end = getTarget().getMainViewInPoint();
		final MapView map = getMap();
		UITools.convertPointToAncestor(target.getMainView(), end, map);
		createStart();
		UITools.convertPointToAncestor(source, start, map);
	}

	abstract public boolean detectCollision(Point p);
}
