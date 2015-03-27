/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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
package org.freeplane.view.swing.map.edge;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;

import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 * Feb 23, 2009
 */
public class HiddenEdgeView extends BezierEdgeView {
	private static Stroke STROKE;

	public HiddenEdgeView(NodeView source, NodeView target, Component paintedComponent) {
	    super(source, target, paintedComponent);
    }

	protected static Stroke getHiddenStroke() {
		if (HiddenEdgeView.STROKE == null) {
			final float dash[] = { 5.0f, 5.0f };
			HiddenEdgeView.STROKE = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 12.0f, dash,
			    0.0f);
		}
		return HiddenEdgeView.STROKE;
	}

	@Override
	public Stroke getStroke() {
		return HiddenEdgeView.getHiddenStroke();
	}

	@Override
	public void paint(final Graphics2D g) {
		if (getSource().isRoot()  || !getTarget().isSelected()) {
			return;
		}
		super.paint(g);
	}

	@Override
	public boolean detectCollision(final Point p) {
		if (!getTarget().isSelected()) {
			return false;
		}
		return super.detectCollision(p);
	}
}
