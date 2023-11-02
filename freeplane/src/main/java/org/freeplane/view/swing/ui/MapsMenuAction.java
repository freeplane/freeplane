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

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.AccelerateableAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.JAutoRadioButtonMenuItem;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;

@SuppressWarnings("serial")
@SelectableAction
class MapsMenuAction extends AFreeplaneAction {
	private static final String MAPS_MENU_ACTION_DOT = "MapsMenuAction.";

	public MapsMenuAction(String command, String title) {
		super(MAPS_MENU_ACTION_DOT + command, title, null);
	}

	public void actionPerformed(final ActionEvent menuEvent) {
		JAutoRadioButtonMenuItem menuItem = (JAutoRadioButtonMenuItem) menuEvent.getSource();
		AccelerateableAction accelerateableAction = (AccelerateableAction) menuItem.getAction();
		AFreeplaneAction action = accelerateableAction.getOriginalAction();
		final String mapId = action.getKey().substring(MAPS_MENU_ACTION_DOT.length());
		UITools.executeWhenNodeHasFocus(new Runnable() {
			@Override
			public void run() {
				try {
					Controller.getCurrentController().getMapViewManager().changeToMapView(mapId);
				}
				catch (IllegalArgumentException ex){
					LogUtils.warn(ex);
				}
			}
		});
	}

}
