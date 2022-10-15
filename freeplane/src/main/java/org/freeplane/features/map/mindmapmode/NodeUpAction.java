/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.map.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.api.LayoutOrientation;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.layout.LayoutController;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

class NodeUpAction extends AFreeplaneAction {
	/**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public NodeUpAction() {
        super("NodeUpAction");
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final ModeController modeController = Controller.getCurrentModeController();
        Controller controller = Controller.getCurrentController();
        IMapSelection selection = controller.getSelection();
        NodeModel selectedNode = selection.getSelected();
        NodeModel selectionRoot = selection.getSelectionRoot();
        if(selectedNode == selectionRoot)
            return;
        LayoutController layoutController = modeController.getExtension(LayoutController.class);
        boolean selectionUsesHorizontalLayout = layoutController.getEffectiveLayoutOrientation(selectedNode.getParentNode(), selection.getFilter()) == LayoutOrientation.LEFT_TO_RIGHT;
        if(selectionUsesHorizontalLayout) {
            ChangeNodeLevelController levelController = modeController.getExtension(ChangeNodeLevelController.class);
            levelController.changeNodeLevelLefts(selectionRoot, selectedNode);
        }
        else {
            MMapController mapController = (MMapController) modeController.getMapController();
            mapController.moveNodesInGivenDirection(selectionRoot, selectedNode, selection.getOrderedSelection(), -1);
        }
    }


}
