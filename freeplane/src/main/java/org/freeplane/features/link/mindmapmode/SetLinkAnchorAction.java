/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Nnamdi Kohn in 2012.
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

package org.freeplane.features.link.mindmapmode;

import java.awt.event.ActionEvent;
import java.io.File;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

@SelectableAction
public class SetLinkAnchorAction extends AFreeplaneAction {
	public SetLinkAnchorAction() {
		super("SetLinkAnchorAction");
	}

	private static final long serialVersionUID = 1L;

	public void actionPerformed(final ActionEvent e) {
		
		/**
		 * @TODO
		 * 
		 * -# implement GUID as reliable nodeID
		 * -# correct tickmark handling in menu (BUG)
		 * -# set tooltip for mouse-over SetLinkAnchorAction in menu
		 * 
		 */
		
		// get reference to selected node
		final Controller controller = Controller.getCurrentController();
		final NodeModel node = controller.getSelection().getSelected();
		
		// get file path of selected node
		File mindmapFile = node.getMap().getFile();
		if(mindmapFile == null) {
			UITools.errorMessage(TextUtils.getRawText("map_not_saved"));
			return;
		}

		// set idString variable according to file and node info
		final String idString = mindmapFile.toURI().toString() + '#' + node.createID();

		// save idString in LinkController
		((MLinkController)(LinkController.getController())).setAnchorID( idString );
	}
	
	@Override
	public void setSelected() {
		final boolean isAnchored = ((MLinkController)(LinkController.getController())).isAnchored();
		setSelected( isAnchored );
	}
}
