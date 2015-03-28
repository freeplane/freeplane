/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.features.styles.mindmapmode;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.ConditionalStyleModel;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyleModel;

/**
 * @author Dimitry Polivaev
 * Jul 21, 2011
 */
public class ManageMapConditionalStylesAction extends AManageConditionalStylesAction{
	
	public static final String NAME = "ManageConditionalStylesAction";
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

	public ManageMapConditionalStylesAction() {
	    super(NAME);
    }

	public void actionPerformed(ActionEvent e) {
		final Controller controller = Controller.getCurrentController();
		final MapModel map = controller.getMap();
		final ConditionalStyleModel conditionalStyleModel = getConditionalStyleModel();
		Component pane = createConditionalStylePane(map, conditionalStyleModel);
		Controller.getCurrentModeController().startTransaction();
		try{
			final int confirmed = JOptionPane.showConfirmDialog(controller.getMapViewManager().getMapViewComponent(), pane, TextUtils.getText(TextUtils.removeMnemonic("ManageConditionalStylesAction.text")), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if(JOptionPane.OK_OPTION == confirmed){
				LogicalStyleController.getController().refreshMap(map);
				Controller.getCurrentModeController().commit();
			}
			else{
				Controller.getCurrentModeController().rollback();

			}
		}
		catch(RuntimeException ex){
			ex.printStackTrace();
			Controller.getCurrentModeController().rollback();
		}
	}

	@Override
	public ConditionalStyleModel getConditionalStyleModel() {
		final Controller controller = Controller.getCurrentController();
		final MapModel map = controller.getMap();
	    final MapStyleModel styleModel = MapStyleModel.getExtension(map);
		final ConditionalStyleModel conditionalStyleModel = styleModel.getConditionalStyleModel();
	    return conditionalStyleModel;
    }
}
