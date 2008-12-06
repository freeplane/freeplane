/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package accessories.plugins;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;

import javax.swing.JViewport;

import org.freeplane.controller.Freeplane;
import org.freeplane.map.tree.view.MapView;

import deprecated.freemind.extensions.ModeControllerHookAdapter;

/**
 * @author foltin
 * @author Dimitry Polivaev
 */
public class FitToPage extends ModeControllerHookAdapter {
	private MapView view;

	/**
	 *
	 */
	public FitToPage() {
		super();
	}

	private void scroll() {
		final Rectangle rect = view.getInnerBounds();
		final Rectangle viewer = view.getVisibleRect();
		view.scrollBy(shift(rect.x, rect.width, viewer.x, viewer.width), shift(
		    rect.y, rect.height, viewer.y, viewer.height));
	}

	private int shift(final int coord1, final int size1, final int coord2,
	                  final int size2) {
		return coord1 - coord2 + (size1 - size2) / 2;
	}

	@Override
	public void startup() {
		super.startup();
		view = Freeplane.getController().getMapView();
		if (view == null) {
			return;
		}
		zoom();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				scroll();
			}
		});
	}

	private void zoom() {
		final Rectangle rect = view.getInnerBounds();
		final double oldZoom = Freeplane.getController().getMapView().getZoom();
		final JViewport viewPort = (JViewport) view.getParent();
		final Dimension viewer = viewPort.getExtentSize();
		double newZoom = viewer.width * oldZoom / (rect.width + 0.0);
		final double heightZoom = viewer.height * oldZoom / (rect.height + 0.0);
		if (heightZoom < newZoom) {
			newZoom = heightZoom;
		}
		Freeplane.getController().getViewController()
		    .setZoom((float) (newZoom));
	}
}
