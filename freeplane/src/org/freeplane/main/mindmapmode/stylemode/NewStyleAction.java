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

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.addins.mapstyle.MapStyleModel;
import org.freeplane.features.mindmapmode.MMapController;

/**
 * @author Dimitry Polivaev
 * 02.10.2009
 */
public class NewStyleAction extends AFreeplaneAction{

	public NewStyleAction(Controller controller) {
	    super("NewStyleAction", controller);
    }

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent e) {
	    final String styleName = JOptionPane.showInputDialog(ResourceBundles.getText("enter new style name"));
	    if(styleName == null){
	    	return;
	    }
	    final MapModel map = getController().getMap();
	    final MapStyleModel styleModel = MapStyleModel.getExtension(map);
	    if (null != styleModel.getStyleNode(styleName)){
	    	UITools.errorMessage(ResourceBundles.getText("style_already_exists"));
	    	return;
	    }
	    final MMapController mapController = (MMapController) getModeController().getMapController();
	    final NodeModel node = new NodeModel(map);
	    node.setUserObject(styleName);
		mapController.insertNode(node, map.getRootNode(), false, false, true);
		mapController.select(node);
		IActor actor = new IActor() {
			public void undo() {
				styleModel.removeStyleNode(node);
			}
			
			public String getDescription() {
				return "NewStyle";
			}
			
			public void act() {
				styleModel.addStyleNode(node);
			}
		};
		getModeController().execute(actor, map);
    }
}
