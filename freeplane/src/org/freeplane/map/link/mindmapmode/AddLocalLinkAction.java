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
package org.freeplane.map.link.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.List;

import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeplaneAction;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.mindmapmode.MModeController;

/**
 * @author foltin
 */
class AddLocalLinkAction extends FreeplaneAction {
	/**
	 */
	public AddLocalLinkAction() {
		super("add_local_link", "images/LinkLocal.png");
	}

	public void actionPerformed(final ActionEvent e) {
		final MModeController modeController = getMModeController();
		final List selecteds = modeController.getSelectedNodes();
		if (selecteds.size() < 2) {
			Controller.getController().errorMessage(
			    modeController.getText("less_than_two_selected_nodes"));
			return;
		}
		final NodeModel target = (NodeModel) selecteds.get(0);
		final String targetId = (target).createID();
		for (int i = 1; i < selecteds.size(); i++) {
			final NodeModel source = (NodeModel) selecteds.get(i);
			((MLinkController) modeController.getLinkController())
			    .setLink(source, ("#" + targetId));
		}
	}
}
