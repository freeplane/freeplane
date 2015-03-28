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
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleModel;

class BubbleMainView extends MainView {

    final static Stroke DEF_STROKE = new BasicStroke();
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public
	Point getLeftPoint() {
		final Point in = new Point(0, getHeight() / 2);
		return in;
	}

	@Override
    public
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
    public
	String getShape() {
		return NodeStyleModel.STYLE_BUBBLE;
	}

	@Override
	public void paintComponent(final Graphics graphics) {
		final Graphics2D g = (Graphics2D) graphics;
		final NodeView nodeView = getNodeView();
		final NodeModel model = nodeView.getModel();
		if (model == null) {
			return;
		}
		final ModeController modeController = getNodeView().getMap().getModeController();
		final Object renderingHint = modeController.getController().getMapViewManager().setEdgesRenderingHint(g);
		paintBackgound(g);
		paintDragOver(g);
		final Color edgeColor = nodeView.getEdgeColor();
		g.setColor(edgeColor);
		g.setStroke(BubbleMainView.DEF_STROKE);
		g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingHint);
		super.paintComponent(g);
	}

	@Override
	protected void paintBackground(final Graphics2D graphics, final Color color) {
		graphics.setColor(color);
		graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
	}
    private static Insets insets = new Insets(3, 3, 3, 3);
    
    @Override
    public Insets getInsets() {
        return BubbleMainView.insets;
    }

    @Override
    public Insets getInsets(Insets insets) {
        return BubbleMainView.insets;
    }
}
