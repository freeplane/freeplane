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
import java.net.URI;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.MenuUtils.MenuEntry;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;

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
			final URI link = NodeLinks.getValidLink(selectedNode);
			if (link != null && LinkController.isMenuItemLink(link)) {
				// remove all icons since we don't know which icon was assigned to the old menuitem
				while (getIconController().removeIcon(selectedNode) > 0)
					;
			}
			final MindIcon icon = menuItem.createMindIcon();
			if (icon != null)
				getIconController().addIcon(selectedNode, icon);
			((MLinkController) LinkController.getController()).setLink(selectedNode, LinkController
				.createMenuItemLink(menuItem.getKey()), false);
		}
	}

	private MIconController getIconController() {
		return (MIconController) IconController.getController();
	}
}
