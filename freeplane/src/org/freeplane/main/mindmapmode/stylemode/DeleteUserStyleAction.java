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

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.MapController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.addins.styles.MapStyleModel;
import org.freeplane.features.mindmapmode.MMapController;

/**
 * @author Dimitry Polivaev
 * 02.10.2009
 */
public class DeleteUserStyleAction extends AFreeplaneAction {
	public DeleteUserStyleAction(Controller controller) {
	    super("DeleteUserStyleAction", controller);
    }

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent e) {
		final ModeController modeController = getModeController();
		final Controller controller = modeController.getController();
		final NodeModel selected = controller.getSelection().getSelected();
		if(selected.depth() < 2){
			UITools.errorMessage(ResourceBundles.getText("can_not_delete_style_group"));
			return;
		}
		if(selected.getUserObject() instanceof NamedObject){
			UITools.errorMessage(ResourceBundles.getText("can_not_delete_predefined_style"));
			return;
		}
		final MapModel map = selected.getMap();
	    final MapStyleModel styleModel = MapStyleModel.getExtension(map);
		final MMapController mapController = (MMapController) modeController.getMapController();
		mapController.deleteNode(selected);
		IActor actor = new IActor() {
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
		getModeController().execute(actor, map);
	}
}
