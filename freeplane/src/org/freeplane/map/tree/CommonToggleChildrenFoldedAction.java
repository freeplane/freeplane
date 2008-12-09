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
package org.freeplane.map.tree;

import java.awt.event.ActionEvent;

import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeMindAction;
import org.freeplane.map.tree.view.NodeView;
import org.freeplane.modes.ModeController;

/**
 * @author foltin
 */
class CommonToggleChildrenFoldedAction extends FreeMindAction {
	final private MapController mapController;

	public CommonToggleChildrenFoldedAction(final MapController mapController) {
		super("toggle_children_folded");
		this.mapController = mapController;
	}

	public void actionPerformed(final ActionEvent e) {
		final ModeController modeController = getModeController();
		final NodeView selected = modeController.getSelectedView();
		final NodeModel model = selected.getModel();
		mapController.toggleFolded(model.getModeController().getMapController()
		    .childrenUnfolded(model));
		modeController.getMapView().selectAsTheOnlyOneSelected(selected);
		Controller.getController().getViewController().obtainFocusForSelected();
	}
}
