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
package org.freeplane.features.common.addins.misc;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.JViewport;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.ActionDescriptor;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.view.swing.map.MapView;

/**
 * @author foltin
 * @author Dimitry Polivaev
 */
@ActionDescriptor(tooltip = "accessories/plugins/FitToPage.properties_documentation", //
name = "accessories/plugins/FitToPage.properties_name", //
locations = { "/menu_bar/view/zoom" })
public class FitToPage extends FreeplaneAction {
	private MapView view;

	/**
	 *
	 */
	public FitToPage() {
		super();
	}

	public void actionPerformed(final ActionEvent e) {
		view = Controller.getController().getMapView();
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

	private void scroll() {
		final Rectangle rect = view.getInnerBounds();
		final Rectangle viewer = view.getVisibleRect();
		view.scrollBy(shift(rect.x, rect.width, viewer.x, viewer.width), shift(rect.y, rect.height,
		    viewer.y, viewer.height));
	}

	private int shift(final int coord1, final int size1, final int coord2, final int size2) {
		return coord1 - coord2 + (size1 - size2) / 2;
	}

	private void zoom() {
		final Rectangle rect = view.getInnerBounds();
		final double oldZoom = view.getZoom();
		final JViewport viewPort = (JViewport) view.getParent();
		final Dimension viewer = viewPort.getExtentSize();
		double newZoom = viewer.width * oldZoom / (rect.width + 0.0);
		final double heightZoom = viewer.height * oldZoom / (rect.height + 0.0);
		if (heightZoom < newZoom) {
			newZoom = heightZoom;
		}
		Controller.getController().getViewController().setZoom((float) (newZoom));
	}
}
