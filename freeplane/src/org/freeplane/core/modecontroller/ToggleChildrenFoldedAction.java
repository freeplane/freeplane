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
package org.freeplane.core.modecontroller;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AFreeplaneAction;

/**
 * @author foltin
 */
class ToggleChildrenFoldedAction extends AFreeplaneAction {
	static final String NAME = "toggleChildrenFolded";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private MapController mapController;

	public ToggleChildrenFoldedAction(final MapController mapController) {
		super("ToggleChildrenFoldedAction", mapController.getModeController().getController());
		this.mapController = mapController;
	}

	public void actionPerformed(final ActionEvent e) {
		final Controller controller = getController();
		final IMapSelection mapSelection = controller.getSelection();
		final NodeModel model = mapSelection.getSelected();
		mapController.toggleFolded(getModeController().getMapController().childrenUnfolded(model));
		mapSelection.selectAsTheOnlyOneSelected(model);
		controller.getViewController().obtainFocusForSelected();
	}
}
