/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 Volker Boerchers
 *
 *  This file's author is Volker Boerchers
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
package org.freeplane.features.controller.help;

import java.awt.event.ActionEvent;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.util.MenuTools;

class HotKeyMapAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	final private Controller controller;

	public HotKeyMapAction(final Controller controller) {
		super(HotKeyMapAction.class.getSimpleName(), controller);
		this.controller = controller;
	}

	public void actionPerformed(final ActionEvent e) {
		try {
			controller.getViewController().setWaitingCursor(true);
			final ModeController modeController = (ModeController) controller.getModeController();
			final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
			final DefaultMutableTreeNode menuEntryTree = MenuTools.createAcceleratebleMenuEntryTree(
			    FreeplaneMenuBar.MENU_BAR_PREFIX, menuBuilder);
			final MapController mapController = getModeController().getMapController();
			// create a useless map to avoid NPE later
			// FIXME: how to avoid that?
			MapModel map = mapController.newMap((NodeModel) null);
			mapController.setSaved(map, true);
			// now do the real thing
			final NodeModel rootNode = mapController.newNode(ResourceBundles.getText("hot_keys"), map);
			MenuTools.insertAsNodeModelRecursively(rootNode, menuEntryTree.children(), mapController);
			// FIXME: how to set the displayed name of the map to something other than "Map1" or the like?
			map = mapController.newMap(rootNode);
			mapController.setSaved(map, true);
		}
		finally {
			getController().getViewController().setWaitingCursor(false);
		}
	}
}
