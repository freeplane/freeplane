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
package org.freeplane.view.swing.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.edge.EdgeStyle;
import org.freeplane.features.common.nodestyle.NodeStyleModel;

class ForkMainView extends MainView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Returns the relative position of the Edge
	 */
	@Override
	int getAlignment() {
		return NodeView.ALIGN_BOTTOM;
	}

	@Override
	Point getCenterPoint() {
		final Point in = new Point(getWidth() / 2, getHeight() / 2);
		return in;
	}

	@Override
	public int getDeltaX() {
		final NodeView nodeView = getNodeView();
		final NodeModel model = nodeView.getModel();
		if (nodeView.getMap().getModeController().getMapController().isFolded(model) && nodeView.isLeft()) {
			return super.getDeltaX() + getZoomedFoldingSymbolHalfWidth() * 3;
		}
		return super.getDeltaX();
	}

	@Override
	Point getLeftPoint() {
		final NodeView nodeView = getNodeView();
		final NodeModel model = nodeView.getModel();
		final EdgeController edgeController = EdgeController.getController(nodeView.getMap().getModeController());
		int edgeWidth = edgeController.getWidth(model);
		final EdgeStyle style = edgeController.getStyle(model);
		edgeWidth = style.getNodeLineWidth(edgeWidth);
		final Point in = new Point(0, getHeight() - edgeWidth / 2 - 1);
		return in;
	}

	@Override
	protected int getMainViewHeightWithFoldingMark() {
		int height = getHeight();
		final NodeView nodeView = getNodeView();
		final NodeModel model = nodeView.getModel();
		if (nodeView.getMap().getModeController().getMapController().isFolded(model)) {
			height += getZoomedFoldingSymbolHalfWidth();
		}
		return height;
	}

	@Override
	protected int getMainViewWidthWithFoldingMark() {
		int width = getWidth();
		final NodeView nodeView = getNodeView();
		final NodeModel model = nodeView.getModel();
		if (nodeView.getMap().getModeController().getMapController().isFolded(model)) {
			width += getZoomedFoldingSymbolHalfWidth() * 2 + getZoomedFoldingSymbolHalfWidth();
		}
		return width;
	}

	@Override
	Point getRightPoint() {
		final NodeView nodeView = getNodeView();
		final NodeModel model = nodeView.getModel();
		final EdgeController edgeController = EdgeController.getController(nodeView.getMap().getModeController());
		int edgeWidth = edgeController.getWidth(model);
		final EdgeStyle style = edgeController.getStyle(model);
		edgeWidth = style.getNodeLineWidth(edgeWidth);
		final Point in = new Point(getWidth() - 1, getHeight() - edgeWidth / 2 - 1);
		return in;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.view.mindmapview.NodeView#getStyle()
	 */
	@Override
	String getStyle() {
		return NodeStyleModel.STYLE_FORK;
	}

	@Override
	public void paint(final Graphics graphics) {
		final Graphics2D g = (Graphics2D) graphics;
		final NodeView nodeView = getNodeView();
		final NodeModel model = nodeView.getModel();
		if (model == null) {
			return;
		}
		final ModeController modeController = getNodeView().getMap().getModeController();
		final Object renderingHint = modeController.getController().getViewController().setEdgesRenderingHint(g);
		paintBackgound(g);
		paintDragOver(g);
		final EdgeController edgeController = EdgeController.getController(modeController);
		int edgeWidth = edgeController.getWidth(model);
		final EdgeStyle style = edgeController.getStyle(model);
		edgeWidth = style.getNodeLineWidth(edgeWidth);
		final Stroke oldStroke = g.getStroke();
		g.setStroke(new BasicStroke(edgeWidth));
		final Color oldColor = g.getColor();
		g.setColor(edgeController.getColor(model));
		g.drawLine(0, getHeight() - edgeWidth / 2 - 1, getWidth(), getHeight() - edgeWidth / 2 - 1);
		g.setColor(oldColor);
		g.setStroke(oldStroke);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
		super.paint(g);
	}

	@Override
	void paintFoldingMark(final NodeView nodeView, final Graphics2D g, final Point p) {
		final int zoomedFoldingSymbolHalfWidth = getZoomedFoldingSymbolHalfWidth();
		if (nodeView.isLeft()) {
			p.x -= zoomedFoldingSymbolHalfWidth;
		}
		else {
			p.x += zoomedFoldingSymbolHalfWidth;
		}
		super.paintFoldingMark(nodeView, g, p);
	}
}
