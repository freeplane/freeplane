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
package org.freeplane.core.frame;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ui.IFreemindPropertyListener;
import org.freeplane.view.swing.map.MapView;

class OptionAntialiasAction extends AbstractAction implements IFreemindPropertyListener {
	OptionAntialiasAction() {
		Controller.getResourceController().addPropertyChangeListener(this);
	}

	public void actionPerformed(final ActionEvent e) {
		final String command = e.getActionCommand();
		changeAntialias(command);
	}

	/**
	 */
	public void changeAntialias(final String command) {
		if (command == null) {
			return;
		}
		if (command.equals("antialias_none")) {
			Controller.getController().getViewController().setAntialiasEdges(false);
			Controller.getController().getViewController().setAntialiasAll(false);
		}
		if (command.equals("antialias_edges")) {
			Controller.getController().getViewController().setAntialiasEdges(true);
			Controller.getController().getViewController().setAntialiasAll(false);
		}
		if (command.equals("antialias_all")) {
			Controller.getController().getViewController().setAntialiasEdges(true);
			Controller.getController().getViewController().setAntialiasAll(true);
		}
		final MapView mapView = Controller.getController().getMapView();
		if (mapView != null) {
			mapView.repaint();
		}
	}

	public void propertyChanged(final String propertyName, final String newValue,
	                            final String oldValue) {
		if (propertyName.equals(ViewController.RESOURCE_ANTIALIAS)) {
			changeAntialias(newValue);
		}
	}
}
