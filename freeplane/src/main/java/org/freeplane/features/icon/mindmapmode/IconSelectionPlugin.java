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
import java.util.Collection;

import javax.swing.Action;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.IconSelectionPopupDialog;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.icon.EmojiIcon;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.IconDescription;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

/**
 * @author adapted to the plugin mechanism by ganzer
 */
public class IconSelectionPlugin extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
    private static final String ADD_EMOJIS_TO_ICON_SELECTOR = "add_emojis_to_icon_selector";

	public IconSelectionPlugin() {
		super("IconSelectionPlugin");
	}

	
	private boolean areEmojisAvailbleFromIconSelector() {
	    return ResourceController.getResourceController().getBooleanProperty(ADD_EMOJIS_TO_ICON_SELECTOR);
    }

	
	public void actionPerformed(final ActionEvent e) {
		final ModeController modeController = Controller.getCurrentModeController();
		ArrayList<IconDescription> actions = new ArrayList<IconDescription>();
		
		final Controller controller = Controller.getCurrentController();

		actions.add((IconDescription) modeController.getAction("RemoveIcon_0_Action"));
		actions.add((IconDescription) modeController.getAction("RemoveIconAction"));
		actions.add((IconDescription) modeController.getAction("RemoveAllIconsAction"));

		final MIconController mIconController = (MIconController) IconController.getController();
		Collection<AFreeplaneAction> iconActions =areEmojisAvailbleFromIconSelector() ? 
				mIconController.getIconActions() : 
					mIconController.getIconActions(icon -> ! (icon instanceof EmojiIcon));
		
		for (AFreeplaneAction aFreeplaneAction : iconActions)
			actions.add((IconDescription) aFreeplaneAction);
		
		final IconSelectionPopupDialog selectionDialog = new IconSelectionPopupDialog(UITools.getCurrentFrame(), actions);
		final NodeModel selected = controller.getSelection().getSelected();
		controller.getMapViewManager().scrollNodeToVisible(selected);
		selectionDialog.setModal(false);
		selectionDialog.setActionListener(evt -> {
			final int result = selectionDialog.getIconIndex();
			if (result >= 0) {
				final Action action = (Action) actions.get(result);
				if(action.isEnabled())
					action.actionPerformed(new ActionEvent(action, 0, NodeModel.NODE_ICON, selectionDialog.getModifiers()));
			}

		});
		selectionDialog.show();
	}
}
