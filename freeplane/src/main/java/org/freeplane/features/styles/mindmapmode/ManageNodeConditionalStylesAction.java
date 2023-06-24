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

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.FilterConditionEditor.Variant;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.ConditionalStyleModel;

/**
 * @author Dimitry Polivaev
 * Jul 21, 2011
 */
public class ManageNodeConditionalStylesAction extends AManageConditionalStylesAction{

	public static final String NAME = "ManageNodeConditionalStylesAction";
	/**
     *
     */
    private static final long serialVersionUID = 1L;

	public ManageNodeConditionalStylesAction() {
	    super(NAME);
    }

    public void actionPerformed(ActionEvent e) {
        final Controller controller = Controller.getCurrentController();
        final MapModel map = controller.getMap();
        final ConditionalStyleModel conditionalStyleModel = getConditionalStyleModel();
        Component pane = createConditionalStylePane(map, conditionalStyleModel, Variant.NODE_CONDITION);
        final ModeController modeController = Controller.getCurrentModeController();

        createAndShowDialog(modeController, conditionalStyleModel, pane,
                            TextUtils.getText(TextUtils.removeMnemonic("ManageNodeConditionalStylesAction.text")));
    }

    protected void handleOkAction(ModeController modeController, ConditionalStyleModel conditionalStyleModel) {
        final IMapSelection selection = modeController.getController().getSelection();
        for (NodeModel node : selection.getSelection()) {
            MLogicalStyleController.getController().setConditionalStyles(node, conditionalStyleModel.clone());
        }
    }

    private ConditionalStyleModel getConditionalStyleModel() {
		final Controller controller = Controller.getCurrentController();
		final NodeModel node = controller.getSelection().getSelected();
		ConditionalStyleModel conditionalStyleModel = node.getExtension(ConditionalStyleModel.class);
		if(conditionalStyleModel == null){
			return new ConditionalStyleModel();
		}
	    return conditionalStyleModel.clone();
    }
}
