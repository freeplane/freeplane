/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.common.nodestyle.NodeStyleController;

class RootMainView extends MainView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean dropAsSibling(final double xCoord) {
		return false;
	}

	/** @return true if should be on the left, false otherwise. */
	@Override
	public boolean dropPosition(final double xCoord) {
		return xCoord < getSize().width * 1 / 2;
	}

	/**
	 * Returns the relative position of the Edge
	 */
	@Override
	int getAlignment() {
		return NodeView.ALIGN_CENTER;
	}

	@Override
	Point getCenterPoint() {
		final Point in = getLeftPoint();
		in.x = getWidth() / 2;
		return in;
	}

	@Override
	Point getLeftPoint() {
		final Point in = new Point(0, getHeight() / 2);
		return in;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.view.mindmapview.NodeView.MainView#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		final Dimension prefSize = super.getPreferredSize();
        if (isPreferredSizeSet()) {
            return prefSize;
        }
		prefSize.width *= 1.1;
		prefSize.height *= 2;
		return prefSize;
	}

	@Override
	Point getRightPoint() {
		final Point in = getLeftPoint();
		in.x = getWidth() - 1;
		return in;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.view.mindmapview.NodeView#getStyle()
	 */
	@Override
	String getStyle() {
		return ResourceController.getResourceController().getProperty(NodeStyleController.RESOURCES_ROOT_NODE_SHAPE);
	}

	@Override
	public void paint(final Graphics graphics) {
		final Graphics2D g = (Graphics2D) graphics;
		if (getNodeView().getModel() == null) {
			return;
		}
		final ModeController modeController = getNodeView().getMap().getModeController();
		final Object renderingHint = modeController.getController().getViewController().setEdgesRenderingHint(g);
		paintBackgound(g);
		paintDragOver(g);
		g.setColor(Color.gray);
		g.setStroke(new BasicStroke(1.0f));
		g.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
		super.paint(g);
	}

	@Override
	protected void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fillOval(1, 1, getWidth() - 2, getHeight() - 2);
	}

	@Override
	public void paintDragOver(final Graphics2D graphics) {
		final int draggedOver = getDraggedOver();
		if (draggedOver == NodeView.DRAGGED_OVER_SON) {
			graphics.setPaint(new GradientPaint(getWidth() / 4, 0, getNodeView().getMap().getBackground(),
			    getWidth() * 3 / 4, 0, NodeView.dragColor));
			graphics.fillRect(getWidth() / 4, 0, getWidth() - 1, getHeight() - 1);
		}
		else if (draggedOver == NodeView.DRAGGED_OVER_SON_LEFT) {
			graphics.setPaint(new GradientPaint(getWidth() * 3 / 4, 0, getNodeView().getMap().getBackground(),
			    getWidth() / 4, 0, NodeView.dragColor));
			graphics.fillRect(0, 0, getWidth() * 3 / 4, getHeight() - 1);
		}
	}
	@Override
	public void setDraggedOver(final Point p) {
		setDraggedOver((dropPosition(p.getX())) ? NodeView.DRAGGED_OVER_SON_LEFT : NodeView.DRAGGED_OVER_SON);
	}
}
