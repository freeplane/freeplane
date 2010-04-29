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
import java.net.URI;
import java.net.URISyntaxException;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.link.LinkController;
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
			final MLinkController linkController = (MLinkController) MLinkController.getController(modeController);
			if (inputValue.equals("")) {
				linkController.setLink(selectedNode, (URI) null, false);
				return;
			}
			try {
				final URI link = LinkController.createURI(inputValue.trim());
				linkController.setLink(selectedNode, link, false);
			}
			catch (final URISyntaxException e1) {
				LogTool.warn(e1);
				UITools.errorMessage(FpStringUtils.format("invalid_uri", inputValue));
				return;
			}
		}
	}
}
