/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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
import org.freeplane.features.common.edge.EdgeStyle;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 * 29.08.2009
 */
public class OutlineEdgeView extends EdgeView {
	public OutlineEdgeView(NodeView target) {
	    super(target);
    }

	@Override
	public boolean detectCollision(Point p) {
		return false;
	}

	protected void createStart() {
		start = getSource().getMainViewInPoint();
		UITools.convertPointToAncestor(getSource().getMainView(), start, getSource());
	}
	@Override
	protected void draw(Graphics2D g) {
		final Color color = getColor();
		g.setColor(color);
		final Stroke stroke = getStroke();
		g.setStroke(stroke);
		g.drawLine(start.x, start.y, start.x, end.y);
		g.drawLine(start.x, end.y, end.x, end.y);
	}
	
	@Override
	protected Stroke getStroke() {
		final EdgeController edgeController = EdgeController.getController(getSource().getMap().getModeController());
		final NodeModel model = getTarget().getModel();
		int edgeWidth = edgeController.getWidth(model);
		final EdgeStyle style = edgeController.getStyle(model);
		edgeWidth = style.getNodeLineWidth(edgeWidth);
		return new BasicStroke(edgeWidth);
	}
}
