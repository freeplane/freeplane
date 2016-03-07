/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.main.mindmapmode.stylemode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.StyleTranslatedObject;

/**
 * @author Dimitry Polivaev
 * 02.10.2009
 */
public class DeleteUserStyleAction extends AFreeplaneAction {
	public DeleteUserStyleAction() {
		super("DeleteUserStyleAction");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void actionPerformed(final ActionEvent e) {
		final ModeController modeController = Controller.getCurrentModeController();
		final Controller controller = modeController.getController();
		final NodeModel selected = controller.getSelection().getSelected();
		if (selected.depth() < 2) {
			UITools.errorMessage(TextUtils.getText("can_not_delete_style_group"));
			return;
		}
		final MapModel map = selected.getMap();
		final MapStyleModel styleModel = MapStyleModel.getExtension(map);
		final NodeModel styleNodeGroup = styleModel.getStyleNodeGroup(selected);
        if (! ((StyleTranslatedObject)styleNodeGroup.getUserObject()).getObject().equals("styles.user-defined")) {
            UITools.errorMessage(TextUtils.getText("can_not_delete_predefined_style"));
            return;
        }
		final MMapController mapController = (MMapController) modeController.getMapController();
		mapController.deleteNode(selected);
		final IActor actor = new IActor() {
			public void undo() {
				styleModel.addStyleNode(selected);
			}

			public String getDescription() {
				return "DeleteStyle";
			}

			public void act() {
				styleModel.removeStyleNode(selected);
			}
		};
		Controller.getCurrentModeController().execute(actor, map);
	}
}
