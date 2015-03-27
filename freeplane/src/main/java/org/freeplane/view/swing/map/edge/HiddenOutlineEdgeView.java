/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Stroke;

import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 * Oct 23, 2011
 */
public class HiddenOutlineEdgeView extends OutlineEdgeView {

	public HiddenOutlineEdgeView(NodeView source, NodeView target, Component paintedComponent) {
	    super(source, target, paintedComponent);
    }

	@Override
    protected Stroke getStroke() {
	    return HiddenEdgeView.getHiddenStroke();
    }

	@Override
    public void paint(Graphics2D g) {
		if (getSource().isRoot()  || !getTarget().isSelected()) {
			return;
		}
	    super.paint(g);
    }
	
	
}
