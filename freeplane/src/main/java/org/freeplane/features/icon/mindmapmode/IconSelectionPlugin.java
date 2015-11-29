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
package org.freeplane.features.icon.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.ListModel;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.IconSelectionPopupDialog;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.icon.IIconInformation;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.IconRegistry;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.ViewController;

/**
 * @author adapted to the plugin mechanism by ganzer
 */
public class IconSelectionPlugin extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public IconSelectionPlugin() {
		super("IconSelectionPlugin");
	}

	public void actionPerformed(final ActionEvent e) {
		final ModeController modeController = Controller.getCurrentModeController();
		ArrayList<IIconInformation> actions = new ArrayList<IIconInformation>();
		
		final Controller controller = Controller.getCurrentController();
		final MapModel map = controller.getMap();
		final IconRegistry iconRegistry = map.getIconRegistry();
		final ListModel usedIcons = iconRegistry.getIconsAsListModel();
		for(int i = 0; i < usedIcons.getSize(); i++){
			final Object icon = usedIcons.getElementAt(i);
			if(icon instanceof MindIcon){
				actions.add(new IconAction((MindIcon) icon));
			}
		}

		final MIconController mIconController = (MIconController) IconController.getController();
		for (AFreeplaneAction aFreeplaneAction : mIconController.getIconActions())
			actions.add((IIconInformation) aFreeplaneAction);
		
		actions.add((IIconInformation) modeController.getAction("RemoveIcon_0_Action"));
		actions.add((IIconInformation) modeController.getAction("RemoveIconAction"));
		actions.add((IIconInformation) modeController.getAction("RemoveAllIconsAction"));

		final IconSelectionPopupDialog selectionDialog = new IconSelectionPopupDialog(UITools.getCurrentFrame(), actions);
		final NodeModel selected = controller.getSelection().getSelected();
		controller.getMapViewManager().scrollNodeToVisible(selected);
		selectionDialog.pack();
		UITools.setDialogLocationRelativeTo(selectionDialog, selected);
		selectionDialog.setModal(true);
		selectionDialog.show();
		final int result = selectionDialog.getResult();
		if (result >= 0) {
			final Action action = (Action) actions.get(result);
			action.actionPerformed(new ActionEvent(action, 0, NodeModel.NODE_ICON, selectionDialog.getModifiers()));
		}
	}
}
