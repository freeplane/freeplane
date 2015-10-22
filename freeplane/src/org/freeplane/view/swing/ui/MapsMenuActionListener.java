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
package org.freeplane.view.swing.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;

class MapsMenuActionListener implements ActionListener {
// // 	final private Controller controller;

	public MapsMenuActionListener(final Controller controller) {
//		this.controller = controller;
	}

	public void actionPerformed(final ActionEvent menuEvent) {
		final String mapId = menuEvent.getActionCommand();
				final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
				final Component selectedComponent = mapViewManager.getSelectedComponent();
				if(selectedComponent != null && ! selectedComponent.hasFocus()){
					selectedComponent.addFocusListener(new  FocusListener() {
						
						public void focusLost(FocusEvent e) {
						}
						
						public void focusGained(FocusEvent e) {
							selectedComponent.removeFocusListener(this);
							mapViewManager.changeToMapView(mapId);
						}
					});
					selectedComponent.requestFocusInWindow();
				}
				else
					mapViewManager.changeToMapView(mapId);
			}

}
