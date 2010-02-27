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
package org.freeplane.features.mindmapmode.link;

import java.awt.event.ActionEvent;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.common.link.LinkController;

/**
 * @author foltin
 */
class AddLocalLinkAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 */
	public AddLocalLinkAction(final Controller controller) {
		super("AddLocalLinkAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final ModeController modeController = getModeController();
		final List selecteds = modeController.getMapController().getSelectedNodes();
		if (selecteds.size() < 2) {
			getController();
			UITools.errorMessage(ResourceBundles.getText("less_than_two_selected_nodes"));
			return;
		}
		final NodeModel target = (NodeModel) selecteds.get(0);
		final String targetId = (target).createID();
		for (int i = 1; i < selecteds.size(); i++) {
			final NodeModel source = (NodeModel) selecteds.get(i);
			((MLinkController) LinkController.getController(modeController)).setLink(source, ("#" + targetId), false);
		}
	}
}
