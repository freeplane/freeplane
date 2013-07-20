/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 Volker Boerchers
 *
 *  This file author is Volker Boerchers
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
 *  along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
 */
package org.freeplane.features.link.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.MenuUtils.MenuEntry;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

class AddMenuItemLinkAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public AddMenuItemLinkAction() {
		super(AddMenuItemLinkAction.class.getSimpleName());
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel selectedNode = Controller.getCurrentModeController().getMapController().getSelectedNode();
		final SelectMenuItemDialog dialog = new SelectMenuItemDialog(selectedNode);
		final MenuEntry menuItem = dialog.getMenuItem();
		if (menuItem != null) {
            ((MLinkController) LinkController.getController()).setLink(selectedNode,
                LinkController.createMenuItemLink(menuItem.getKey()), LinkController.LINK_ABSOLUTE);
		}
	}
}
