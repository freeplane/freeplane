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
import java.net.URI;
import java.net.URISyntaxException;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class MakeLinkFromAnchorAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public MakeLinkFromAnchorAction() {
		super("MakeLinkFromAnchorAction");
	}

	public void actionPerformed(final ActionEvent e) {

		// get reference current controller
		final ModeController modeControllerForSelectedMap = Controller.getCurrentModeController();

		// get reference of selected node
		final NodeModel selectedNode = modeControllerForSelectedMap.getMapController().getSelectedNode();

		// get file path of selected node
		File selectedMapFilePath = selectedNode.getMap().getFile();
		if(selectedMapFilePath == null) {
			UITools.errorMessage(TextUtils.getRawText("map_not_saved"));
			return;
		}
		
		// get ID (consisting of FILE PATH and NODE ID) of selected node
		final String selectedID = selectedMapFilePath.toURI().toString() + '#' + selectedNode.createID();

		// get anchorID from MLinkController
		final String anchorID = ((MLinkController)(LinkController.getController())).getAnchorID();

		// check if anchorID valid (should be null when file is closed or anchor is cleared)
		if( anchorID == null) {
			return;
		}
		
		// extract anchorMapFileName and anchorNodeID from anchorID
/*		final int idxMapFileName = anchorID.indexOf("/") +1;
		final int idxNodeID = anchorID.indexOf("#") + 1;
		final String anchorMapFileName = anchorID.substring(idxMapFileName, idxNodeID-1);
		final String anchorNodeID = anchorID.substring(idxNodeID);
*/		
		
		// open anchored map
		final MLinkController linkController_selected = (MLinkController) MLinkController.getController();
		try {
			final URI linkToAnchorNode = LinkController.createURI(anchorID.trim());
			linkController_selected.loadURI(linkToAnchorNode);
		}
		catch (final URISyntaxException e1) {
			LogUtils.warn(e1);
//			UITools.errorMessage(TextUtils.format("invalid_uri", link));
			return;
		}

		// get reference of anchor node within anchor map
		final NodeModel anchorNode = modeControllerForSelectedMap.getMapController().getSelectedNode();

		// set link in anchored node within anchored map
		final MLinkController linkController_anchored = (MLinkController) MLinkController.getController();
		try {
			final URI linkToCurrentNode = LinkController.createURI(selectedID.trim());
			linkController_anchored.setLink(anchorNode, linkToCurrentNode, false);
		}
		catch (final URISyntaxException e1) {
			LogUtils.warn(e1);
//			UITools.errorMessage(TextUtils.format("invalid_uri", link));
			return;
		}

		// re-open formerly selected map
		try {
			final URI linkBackToSelectedNode = LinkController.createURI(selectedID.trim());
			linkController_anchored.loadURI(linkBackToSelectedNode);
		}
		catch (final URISyntaxException e1) {
			LogUtils.warn(e1);
//			UITools.errorMessage(TextUtils.format("invalid_uri", link));
			return;
		}

	}
}
