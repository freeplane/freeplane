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
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

@EnabledAction
public class MakeLinkFromAnchorAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public MakeLinkFromAnchorAction() {
		super("MakeLinkFromAnchorAction");
	}

	public void actionPerformed(final ActionEvent e) {

		// get reference to current modeController
		final ModeController modeControllerForSelectedMap = Controller.getCurrentModeController();

		// get reference of selected node (target)
		final NodeModel targetNode = modeControllerForSelectedMap.getMapController().getSelectedNode();
		// get file path of selected node (target)
		final File targetMapFile = targetNode.getMap().getFile();
		if(targetMapFile == null) {
			UITools.errorMessage(TextUtils.getRawText("map_not_saved"));
			return;
		}
		// extract file name string (URI) for target
		final String targetMapFileNameURI = targetMapFile.toURI().toString();
		// extract file name string for target
		final String targetMapFileName = targetMapFileNameURI.substring(targetMapFileNameURI.indexOf("/")+1);
		// get ID (consisting of fileName and nodeID) of selected node (as targetID)
		final String targetID = targetMapFile.toURI().toString() + '#' + targetNode.createID();

		// get anchorID (as sourceID) from MLinkController
		final String sourceID = ((MLinkController)(LinkController.getController())).getAnchorID();
		// check if anchorID valid (should be null when file is closed or anchor is cleared)
		if( sourceID == null) {
			return;
		}
		// extract anchorMapFileName (source)
		final String sourceMapFileName = sourceID.substring( sourceID.indexOf("/") +1, sourceID.indexOf("#") );
		
		// check if target and source reside within same map
		if( targetMapFileName.equals(sourceMapFileName) ) {
		
			// get link controller
			final MLinkController linkController = (MLinkController) MLinkController.getController();
			
			// get nodeID of anchored node (source)
			final String sourceNodeID = sourceID.substring( sourceID.indexOf("#")+1 );
			
			// get reference to node from ID-String (source)
			final NodeModel sourceNode = modeControllerForSelectedMap.getMapController().getNodeFromID(sourceNodeID);

			// insert only targetNodeID as link
			linkController.setLink(sourceNode, targetID.substring(targetID.indexOf("#")), false);
			
		} else {
		
			// navigate to anchored map (source)
			final MLinkController linkController_selected = (MLinkController) MLinkController.getController();
			try {
				final URI linkToAnchorNode = LinkController.createURI(sourceID.trim());
				linkController_selected.loadURI(linkToAnchorNode);
			}
			catch (final URISyntaxException e1) {
				LogUtils.warn(e1);
	//			UITools.errorMessage(TextUtils.format("invalid_uri", link));
				return;
			}

			// get reference of anchor node within anchor map
			final NodeModel sourceNode = modeControllerForSelectedMap.getMapController().getSelectedNode();
	
			// set link in anchored node within anchored map
			final MLinkController linkController_anchored = (MLinkController) MLinkController.getController();
			try {
				final URI linkToCurrentNode = LinkController.createURI(targetID.trim());
				linkController_anchored.setLink(sourceNode, linkToCurrentNode, false);
			}
			catch (final URISyntaxException e1) {
				LogUtils.warn(e1);
	//			UITools.errorMessage(TextUtils.format("invalid_uri", link));
				return;
			}
	
			// re-navigate to target map
			try {
				final URI linkBackToSelectedNode = LinkController.createURI(targetID.trim());
				linkController_anchored.loadURI(linkBackToSelectedNode);
			}
			catch (final URISyntaxException e1) {
				LogUtils.warn(e1);
	//			UITools.errorMessage(TextUtils.format("invalid_uri", link));
				return;
			}
		}
	}
	@Override
	public void setEnabled() {
		final boolean isAnchored = ((MLinkController)(LinkController.getController())).isAnchored();
		setEnabled( isAnchored );
	}
}
