/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 Volker Boerchers
 *
 *  This file author is Volker Boerchers
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
 *  along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
 */
package org.freeplane.features.link.mindmapmode;

import java.awt.event.ActionEvent;
import java.net.URI;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

class AddExecutionLinkAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public AddExecutionLinkAction() {
		super(AddExecutionLinkAction.class.getSimpleName());
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel selectedNode = Controller.getCurrentModeController().getMapController().getSelectedNode();
		String linkAsString;
		final URI link = NodeLinks.getLink(selectedNode);
		if (link != null && LinkController.isSpecialLink(LinkController.EXECUTE_APP_SCHEME, link))
			linkAsString = LinkController.parseSpecialLink(link);
		else
			linkAsString = "";
		final String content = UITools.showInputDialog(Controller.getCurrentController().getSelection().getSelected(),
		    TextUtils.getText("enter_command"), linkAsString);

		final MLinkController linkController = (MLinkController) LinkController.getController();
		if (content != null) {
			final URI newLink = content.isEmpty() ? null : LinkController.createItemLink(LinkController.EXECUTE_APP_SCHEME, content);
			linkController
			    .setLink(selectedNode, newLink,
			        LinkController.LINK_ABSOLUTE);
		}
	}
}
