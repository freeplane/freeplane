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
package org.freeplane.view.swing.map.link;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.link.ArrowType;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.ConnectorModel.Shape;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.map.edge.EdgeView;
import org.freeplane.view.swing.map.edge.EdgeViewFactory;

/**
 * @author Dimitry Polivaev
 * 09.08.2009
 */
public class EdgeLinkView extends AConnectorView {
	private final EdgeView edgeView;

	public EdgeLinkView(final ConnectorModel model, final ModeController modeController, final NodeView source,
	                    final NodeView target) {
		super(model, source, target);
		final MapView map = source.getMap();
		if (map.getLayoutType() == MapViewLayout.OUTLINE) {
			edgeView = new OutlineLinkView(source, target, map);
		}
		else{
			edgeView = EdgeViewFactory.getInstance().getEdge(source, target, map);
		}
		Color color;
		if (Shape.EDGE_LIKE.equals(model.getShape())) {
			color = edgeView.getColor().darker();
		}
		else {
			final LinkController linkController = LinkController.getController(modeController);
			color = linkController.getColor(connectorModel);
			final int alpha = linkController.getAlpha(connectorModel);
			color =  ColorUtils.alphaToColor(alpha, color);
			final int width = linkController.getWidth(model);
			edgeView.setWidth(width);
		}
		edgeView.setColor(color);
	}

	public boolean detectCollision(final Point p, final boolean selectedOnly) {
		if (selectedOnly) {
			final NodeView source = edgeView.getSource();
			if ((source == null || !source.isSelected())) {
				final NodeView target = edgeView.getTarget();
				if ((target == null || !target.isSelected())) {
					return false;
				}
			}
		}
		return edgeView.detectCollision(p);
	}

	public ConnectorModel getModel() {
		return connectorModel;
	}

	public void increaseBounds(final Rectangle innerBounds) {
		//edge link does not increase inner bounds 
	}

	public void paint(final Graphics graphics) {
		edgeView.paint((Graphics2D) graphics);
		if(Shape.EDGE_LIKE.equals(connectorModel.getShape())){
			return;
		}
		if (isSourceVisible() && !connectorModel.getStartArrow().equals(ArrowType.NONE)) {
			Point p1 = edgeView.getStart();
			Point p2 = new Point(p1);
			p2.translate(5, 0);
			paintArrow(graphics, p2, p1);
		}
		if (isTargetVisible() && !connectorModel.getEndArrow().equals(ArrowType.NONE)) {
			Point p1 = edgeView.getEnd();
			Point p2 = new Point(p1);
			p2.translate(5, 0);
			paintArrow(graphics, p2, p1);
		}
		
	}

	private void paintArrow(final Graphics graphics, Point from, Point to) {
	    paintArrow(from, to, (Graphics2D)graphics, getZoom() * 10);
    }
}
