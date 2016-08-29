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
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;

import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 * 29.08.2009
 */
public class OutlineEdgeView extends EdgeView {
	public OutlineEdgeView(NodeView source, NodeView target, Component paintedComponent) {
	    super(source, target, paintedComponent);
    }

	@Override
	public boolean detectCollision(final Point p) {
		return false;
	}

	@Override
	protected void createStart() {
		start = getSource().getMainView().getLeftPoint();
		end = getTarget().getMainView().getLeftPoint();
	}

	@Override
	protected void draw(final Graphics2D g) {
		final Color color = getColor();
		g.setColor(color);
		final Stroke stroke = getStroke();
		g.setStroke(stroke);
		g.drawLine(start.x, start.y, start.x, end.y);
		g.drawLine(start.x, end.y, end.x, end.y);
		if(getTarget().isSummary()){
			final int gap = getWidth();
			final int y1 = end.y + gap * 13/8;
			g.drawLine(start.x, start.y, start.x, y1);
			int x2 = end.x;
			if(NodeStyleModel.Shape.fork.equals(getTarget().getMainView().getShapeConfiguration().getShape()))
				x2 += getTarget().getContent().getWidth();
			g.drawLine(start.x, y1, x2, y1);
		}
	}

	@Override
	protected Stroke getStroke() {
		final NodeView nodeView = getTarget();
		final MainView mainView = nodeView.getMainView();
		final float nodeLineWidth = mainView.getUnzoomedEdgeWidth();
		final float zoomedLineWidth = nodeView.getMap().getZoom() * nodeLineWidth;
		float strokeWidth =  Math.max(zoomedLineWidth, 1);
		return new BasicStroke(strokeWidth);
	}
}
