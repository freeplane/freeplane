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
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.addins.styles.MapStyleModel;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.map.MMapController;

/**
 * @author Dimitry Polivaev
 * 02.10.2009
 */
public class DeleteLevelStyleAction extends AFreeplaneAction {
	public DeleteLevelStyleAction(Controller controller) {
	    super("DeleteLevelStyleAction", controller);
    }

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent e) {
		final ModeController modeController = getModeController();
	    final MapModel map = getController().getMap();
		final NodeModel levelStyleParentNode = getLevelStyleParentNode(map);
	    final int childNumber = levelStyleParentNode.getChildCount() - 1;
		if(childNumber < 1){
			UITools.errorMessage(ResourceBundles.getText("can_not_delete_root_style"));
			return;
		}
		final String styleName = "AutomaticLayout.level," + childNumber;
	    final NamedObject styleObject = NamedObject.formatText(styleName);
	    final MapStyleModel styleModel = MapStyleModel.getExtension(map);
		final MMapController mapController = (MMapController) modeController.getMapController();
		final NodeModel node = styleModel.getStyleNode(styleObject);
		mapController.deleteNode(node);
		IActor actor = new IActor() {
			public void undo() {
				styleModel.addStyleNode(node);
			}
			
			public String getDescription() {
				return "DeleteStyle";
			}
			
			public void act() {
				styleModel.removeStyleNode(node);
			}
		};
		getModeController().execute(actor, map);
	}
	private NodeModel getLevelStyleParentNode(final MapModel map) {
	    return (NodeModel) map.getRootNode().getChildAt(1);
    }
}
