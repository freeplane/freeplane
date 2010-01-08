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
package org.freeplane.features.mindmapmode.addins;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.Action;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.icon.IconController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.ui.components.IconSelectionPopupDialog;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.mindmapmode.icon.MIconController;

/**
 * @author adapted to the plugin mechanism by ganzer
 */
@ActionLocationDescriptor(locations = { "/menu_bar/icons/actions" }, //
accelerator = "control F2")
public class IconSelectionPlugin extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 */
	public IconSelectionPlugin(final Controller controller) {
		super("IconSelectionPlugin", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final ModeController modeController = getModeController();
		final Vector actions = new Vector();
		final Collection<AFreeplaneAction> iconActions = ((MIconController) IconController
		    .getController(modeController)).getIconActions();
		actions.addAll(iconActions);
		actions.add(modeController.getAction("RemoveIcon_0_Action"));
		actions.add(modeController.getAction("RemoveIconAction"));
		actions.add(modeController.getAction("RemoveAllIconsAction"));
		final ViewController viewController = getController().getViewController();
		final IconSelectionPopupDialog selectionDialog = new IconSelectionPopupDialog(viewController.getJFrame(),
		    actions);
		final NodeModel selected = getController().getSelection().getSelected();
		viewController.scrollNodeToVisible(selected);
		selectionDialog.pack();
		UITools.setDialogLocationRelativeTo(selectionDialog, getController(), selected);
		selectionDialog.setModal(true);
		selectionDialog.show();
		final int result = selectionDialog.getResult();
		if (result >= 0) {
			final Action action = (Action) actions.get(result);
			action.actionPerformed(new ActionEvent(action, 0, NodeModel.NODE_ICON, selectionDialog.getModifiers()));
		}
	}
}
