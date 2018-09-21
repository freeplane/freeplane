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
package org.freeplane.features.map;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 * Jun 5, 2011
 */
public class GotoNodeAction extends AFreeplaneAction {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public GotoNodeAction() {
        super("GotoNodeAction");
    }

    @Override
	public void actionPerformed(ActionEvent e) {
        final Controller controller = Controller.getCurrentController();
        final IMapSelection selection = controller.getSelection();
        final NodeModel node = selection.getSelected();
        final String id = UITools.showInputDialog(node, TextUtils.getText("enter_node_id"), (String)getValue(Action.NAME), JOptionPane.QUESTION_MESSAGE);
        if(id == null || "".equals(id))
            return;
        final NodeModel nodeForID = controller.getMap().getNodeForID(id);
        if(nodeForID == null)
            return;
        controller.getModeController().getMapController().displayNode(nodeForID);
        selection.selectAsTheOnlyOneSelected(nodeForID);
    }
}
