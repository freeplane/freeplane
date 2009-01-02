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

import org.freeplane.core.map.NodeModel;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.view.IMapView;
import org.freeplane.map.edge.EdgeController;
import org.freeplane.map.edge.EdgeModel;
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
			EdgeView.ECLIPSED_STROKE = new BasicStroke(3.0f, BasicStroke.CAP_BUTT,
			    BasicStroke.JOIN_MITER, 12.0f, dash, 0.0f);
		}
		return EdgeView.ECLIPSED_STROKE;
	}

	protected NodeView source;
	protected Point start, end;
	private NodeView target;

	protected void createEnd() {
		end = getTarget().getMainViewInPoint();
		UITools.convertPointToAncestor(target.getMainView(), end, source);
	}

	protected void createStart() {
		start = source.getMainViewOutPoint(getTarget(), end);
		UITools.convertPointToAncestor(source.getMainView(), start, source);
	}

	public Color getColor() {
		final NodeModel model = target.getModel();
		final Color edgeColor = EdgeController.getController(model.getModeController()).getColor(model);
		return edgeColor;
	}

	protected IMapView getMap() {
		return getTarget().getMap();
	}

	/**
	 * @return Returns the source.
	 */
	protected NodeView getSource() {
		return source;
	}

	public Stroke getStroke() {
		final int width = getWidth();
		if (width == EdgeModel.WIDTH_THIN) {
			return EdgeView.DEF_STROKE;
		}
		return new BasicStroke(width * getMap().getZoom(), BasicStroke.CAP_BUTT,
		    BasicStroke.JOIN_MITER);
	}

	/**
	 * @return Returns the target.
	 */
	protected NodeView getTarget() {
		return target;
	}

	public int getWidth() {
		final NodeModel model = getTarget().getModel();
		final int width = EdgeController.getController(model.getModeController()).getWidth(model);
		return width;
	}

	protected boolean isTargetEclipsed() {
		return getTarget().isParentHidden();
	}

	abstract protected void paint(Graphics2D g);

	/**
	 * This should be a task of MindMapLayout start,end must be initialized...
	 *
	 * @param target
	 */
	public void paint(final NodeView target, final Graphics2D g) {
		source = target.getVisibleParentView();
		this.target = target;
		createEnd();
		createStart();
		paint(g);
		source = null;
		this.target = null;
	}

	protected void reset() {
		source = null;
		target = null;
	}
}
