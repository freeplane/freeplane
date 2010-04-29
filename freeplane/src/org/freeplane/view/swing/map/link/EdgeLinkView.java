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

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.features.common.addins.mapstyle.MapViewLayout;
import org.freeplane.features.common.link.ArrowType;
import org.freeplane.features.common.link.ConnectorModel;
import org.freeplane.features.common.link.LinkController;
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
		if (source.getMap().getLayoutType() == MapViewLayout.OUTLINE) {
			edgeView = new OutlineLinkView(source, target);
		}
		else{
			edgeView = EdgeViewFactory.getInstance().getEdge(source, target);
		}
		Color color;
		if (model.isEdgeLike()) {
			color = edgeView.getColor().darker();
		}
		else {
			final LinkController controller = LinkController.getController(modeController);
			color = controller.getColor(model);
			final int width = controller.getWidth(model);
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
		if(connectorModel.isEdgeLike()){
			return;
		}
		if (isSourceVisible() && !connectorModel.getStartArrow().equals(ArrowType.NONE)) {
			Point p1 = edgeView.getStart();
			Point p2 = new Point(p1);
			p2.translate(5, 0);
			paintArrow(p1, p2, (Graphics2D)graphics, getZoom() * 7);
		}
		if (isTargetVisible() && !connectorModel.getEndArrow().equals(ArrowType.NONE)) {
			Point p1 = edgeView.getEnd();
			Point p2 = new Point(p1);
			p2.translate(5, 0);
			paintArrow(p1, p2, (Graphics2D)graphics, getZoom() * 7);
		}
		
	}
}
