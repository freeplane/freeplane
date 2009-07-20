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
package org.freeplane.features.mindmapmode.link;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.link.NodeLinks;

class SetLinkByTextFieldAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SetLinkByTextFieldAction(final Controller controller) {
		super("SetLinkByTextFieldAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final ModeController modeController = getModeController();
		final NodeModel selectedNode = modeController.getMapController().getSelectedNode();
		final String inputValue = UITools.showInputDialog(getController(),
		    getController().getSelection().getSelected(), ResourceBundles.getText("edit_link_manually"), NodeLinks
		        .getLinkAsString(selectedNode));
		if (inputValue != null) {
			if (inputValue.equals("")) {
				setLink(selectedNode, null);
				return;
			}
			URI link;
			try {
				link = new URI(inputValue);
			}
			catch (final URISyntaxException e1) {
				try {
					link = new URI(null, null, inputValue, null);
				}
				catch (URISyntaxException e2) {
					LogTool.warn(e1);
					UITools.errorMessage("wrong URI " + inputValue);
					return;
				} 
			}
			setLink(selectedNode, link);
		}
	}

	void setLink(final NodeModel node, final URI link) {
		final IActor actor = new IActor() {
			private URI oldlink;
			private String oldTargetID;

			public void act() {
				NodeLinks links = NodeLinks.getLinkExtension(node);
				if (links != null) {
					oldlink = links.getHyperLink();
					oldTargetID = links.removeLocalHyperLink(node);
				}
				else {
					links = NodeLinks.createLinkExtension(node);
				}
				if (link != null && link.toString().startsWith("#")) {
					links.setLocalHyperlink(node, link.toString().substring(1));
				}
				links.setHyperLink(link);
				getModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setLink";
			}

			public void undo() {
				final NodeLinks links = NodeLinks.getLinkExtension(node);
				links.setLocalHyperlink(node, oldTargetID);
				links.setHyperLink(oldlink);
				getModeController().getMapController().nodeChanged(node);
			}
		};
		getModeController().execute(actor);
	}
}
