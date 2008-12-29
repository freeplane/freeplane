/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.addins.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.Action;

import org.freeplane.controller.ActionDescriptor;
import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeplaneAction;
import org.freeplane.map.icon.mindmapnode.MIconController;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.map.tree.view.NodeView;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.ui.components.IconSelectionPopupDialog;
import org.freeplane.ui.components.UITools;

/**
 * @author adapted to the plugin mechanism by ganzer
 */
@ActionDescriptor(tooltip = "accessories/plugins/IconSelectionPlugin.properties_documentation", //
name = "accessories/plugins/IconSelectionPlugin.properties_name", //
keyStroke = "keystroke_accessories/plugins/IconSelectionPlugin.properties.properties_key", //
iconPath = "accessories/plugins/icons/kalzium.png", //
locations = { "/menu_bar/insert/icons" })
public class IconSelectionPlugin extends FreeplaneAction {
	/**
	 */
	public IconSelectionPlugin() {
		super();
	}

	public void actionPerformed(final ActionEvent e) {
		final MModeController modeController = (MModeController) getModeController();
		final NodeView focussed = modeController.getSelectedView();
		final Vector actions = new Vector();
		final Collection<Action> iconActions = ((MIconController) modeController
		    .getIconController()).getIconActions();
		actions.addAll(iconActions);
		actions.add(modeController.getAction("removeLastIconAction"));
		actions.add(modeController.getAction("removeAllIconsAction"));
		final IconSelectionPopupDialog selectionDialog = new IconSelectionPopupDialog(Controller
		    .getController().getViewController().getJFrame(), actions);
		final MapView mapView = modeController.getMapView();
		mapView.scrollNodeToVisible(focussed, 0);
		selectionDialog.pack();
		UITools.setDialogLocationRelativeTo(selectionDialog, focussed);
		selectionDialog.setModal(true);
		selectionDialog.show();
		final int result = selectionDialog.getResult();
		if (result >= 0) {
			final Action action = (Action) actions.get(result);
			action.actionPerformed(new ActionEvent(action, 0, "icon", selectionDialog
			    .getModifiers()));
		}
	}
}
