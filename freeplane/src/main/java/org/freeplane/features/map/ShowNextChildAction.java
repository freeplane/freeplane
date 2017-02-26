/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Dimitry
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
package org.freeplane.features.map;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;

/**
 * @author Dimitry Polivaev
 * 22.04.2012
 */
@SuppressWarnings("serial")
@EnabledAction(checkOnNodeChange = true)
public class ShowNextChildAction extends AFreeplaneAction {
	public ShowNextChildAction() {
	    super("ShowNextChildAction");
    }

	public void actionPerformed(ActionEvent e) {
		final Controller controller = Controller.getCurrentController();
		final NodeModel selected = controller.getSelection().getSelected();
		final MapController mapController = controller.getModeController().getMapController();
		mapController.showNextChild(selected);
	}

	@Override
    public void setEnabled() {
		final Controller controller = Controller.getCurrentController();
		if(controller.getSelection() != null) {
			final NodeModel selected = controller.getSelection().getSelected();
			final MapController mapController = controller.getModeController().getMapController();
		    final IMapViewManager mapViewManager = controller.getMapViewManager();
			super.setEnabled(mapViewManager.isFoldedOnCurrentView(selected) || mapViewManager.hasHiddenChildren(selected));
		}
		else {
			super.setEnabled(false);
		}
    }
	
	
}
