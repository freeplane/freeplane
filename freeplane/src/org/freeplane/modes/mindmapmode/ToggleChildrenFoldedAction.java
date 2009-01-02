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
package org.freeplane.modes.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.core.view.IMapView;

class ToggleChildrenFoldedAction extends FreeplaneAction {
	public ToggleChildrenFoldedAction() {
		super("toggle_children_folded");
	}

	public void actionPerformed(final ActionEvent e) {
		final MModeController modeController = MModeController.getMModeController();
		final NodeModel selected = modeController.getSelectedNode();
		((ToggleFoldedAction) getModeController().getAction("toggleFolded")).toggleFolded(selected
		    .getModeController().getMapController().childrenUnfolded(selected));
		final IMapView mapView = modeController.getMapView();
		mapView.selectAsTheOnlyOneSelected(mapView.getNodeView(selected));
		Controller.getController().getViewController().obtainFocusForSelected();
	}
}
