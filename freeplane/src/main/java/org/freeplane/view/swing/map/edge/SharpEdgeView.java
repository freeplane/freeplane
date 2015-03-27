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

/**
 * @author Dimitry Polivaev
 * May 8, 2011
 */
import java.awt.Component;

import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;

public abstract class SharpEdgeView extends EdgeView {
    private int deltaY;
    private int deltaX;

    protected int getDeltaY() {
        return deltaY;
    }

    protected int getDeltaX() {
        return deltaX;
    }

	public SharpEdgeView(NodeView source, NodeView target, Component paintedComponent) {
	    super(source, target, paintedComponent);
    }

    @Override
    protected void createStart() {
             super.createStart();
             final int delta = getMap().getZoomed(getWidth() + 1);
             if (getSource().isRoot()) {
                 final MainView mainView = getSource().getMainView();
                 final double w = mainView.getWidth() / 2;
                 final double x0 = start.x - w;
                 final double w2 = w * w;
                 final double x02 = x0 * x0;
                 if (Double.compare(w2, x02) == 0) {
                     deltaX = 0;
                     deltaY = delta;
                 }
                 else {
                     final int h = mainView.getHeight() / 2;
                     final int y0 = start.y - h;
                     final double k = h / w * x0 / Math.sqrt(w2 - x02);
                     final double dx = delta / Math.sqrt(1 + k * k);
                     deltaX = (int) dx;
                     deltaY = (int) (k * dx);
                     if (y0 > 0) {
                         deltaY = -deltaY;
                     }
                 }
             }
             else {
                 deltaX = 0;
                 deltaY = delta;
             }
       }
}
