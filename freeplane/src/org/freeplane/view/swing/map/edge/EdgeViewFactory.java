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

import java.awt.Component;

import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 * 09.08.2009
 */
public class EdgeViewFactory {
	final private static EdgeViewFactory instance = new EdgeViewFactory();

	public EdgeView getEdge(final NodeView source, final NodeView target, Component paintedComponent) {
		final EdgeStyle edgeStyle = target.getEdgeStyle();
		if (source.getMap().getLayoutType() == MapViewLayout.OUTLINE) {
			if(edgeStyle.equals(EdgeStyle.EDGESTYLE_HIDDEN))
				return new HiddenOutlineEdgeView(source, target, paintedComponent);
			return new OutlineEdgeView(source, target, paintedComponent);
		}
		if (edgeStyle.equals(EdgeStyle.EDGESTYLE_LINEAR)) {
			return new LinearEdgeView(source, target, paintedComponent);
		}
		else if (edgeStyle.equals(EdgeStyle.EDGESTYLE_BEZIER)) {
			return new BezierEdgeView(source, target, paintedComponent);
		}
		else if (edgeStyle.equals(EdgeStyle.EDGESTYLE_SUMMARY)) {
            return new SummaryEdgeView(source, target, paintedComponent);
        }
		else if (edgeStyle.equals(EdgeStyle.EDGESTYLE_SHARP_LINEAR)) {
			return new SharpLinearEdgeView(source, target, paintedComponent);
		}
		else if (edgeStyle.equals(EdgeStyle.EDGESTYLE_SHARP_BEZIER)) {
			return new SharpBezierEdgeView(source, target, paintedComponent);
		}
		else if (edgeStyle.equals(EdgeStyle.EDGESTYLE_HORIZONTAL)) {
			return new HorizontalEdgeView(source, target, paintedComponent);
		}
		else if (edgeStyle.equals(EdgeStyle.EDGESTYLE_HIDDEN)) {
			return new HiddenEdgeView(source, target, paintedComponent);
		}
		else {
			System.err.println("Unknown Edge Type.");
			return new LinearEdgeView(source, target, paintedComponent);
		}
	}

	public static EdgeViewFactory getInstance() {
		return instance;
	}
}
