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

@EnabledAction(checkOnNodeChange=true)
public class MakeLinkFromAnchorAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public MakeLinkFromAnchorAction() {
		super("MakeLinkFromAnchorAction");
	}

	public void actionPerformed(final ActionEvent e) {

		final ModeController modeControllerForSelectedMap = Controller.getCurrentModeController();

		final NodeModel targetNode = modeControllerForSelectedMap.getMapController().getSelectedNode();
		final File targetMapFile = targetNode.getMap().getFile();
		if(targetMapFile == null) {
			UITools.errorMessage(TextUtils.getRawText("map_not_saved"));
			return;
		}
		final String targetMapFileNameURI = targetMapFile.toURI().toString();
		final String targetMapFileName = targetMapFileNameURI.substring(targetMapFileNameURI.indexOf("/")+1);
		final String targetID = targetMapFile.toURI().toString() + '#' + targetNode.createID();

		final String sourceID = ((MLinkController)(LinkController.getController())).getAnchorID();
		if( sourceID == null) {
			return;
		}
		final String sourceMapFileName = sourceID.substring( sourceID.indexOf("/") +1, sourceID.indexOf("#") );
		
		if( targetMapFileName.equals(sourceMapFileName) ) {
		
			final MLinkController linkController = (MLinkController) MLinkController.getController();
			
			final String sourceNodeID = sourceID.substring( sourceID.indexOf("#")+1 );
			
			final NodeModel sourceNode = modeControllerForSelectedMap.getMapController().getNodeFromID(sourceNodeID);

			linkController.setLinkTypeDependantLink(sourceNode, targetID.substring(targetID.indexOf("#")));
			
		} else {
		
			final MLinkController linkController_selected = (MLinkController) MLinkController.getController();
			try {
				final URI linkToAnchorNode = LinkController.createURI(sourceID.trim());
				linkController_selected.loadURI(linkToAnchorNode);
			}
			catch (final URISyntaxException e1) {
				LogUtils.warn(e1);
				return;
			}

			final NodeModel sourceNode = modeControllerForSelectedMap.getMapController().getSelectedNode();
	
			final MLinkController linkController_anchored = (MLinkController) MLinkController.getController();
			try {
				final URI linkToCurrentNode = LinkController.createURI(targetID.trim());
				linkController_anchored.setLinkTypeDependantLink(sourceNode, linkToCurrentNode);
			}
			catch (final URISyntaxException e1) {
				LogUtils.warn(e1);
				return;
			}
	
			try {
				final URI linkBackToSelectedNode = LinkController.createURI(targetID.trim());
				linkController_anchored.loadURI(linkBackToSelectedNode);
			}
			catch (final URISyntaxException e1) {
				LogUtils.warn(e1);
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
