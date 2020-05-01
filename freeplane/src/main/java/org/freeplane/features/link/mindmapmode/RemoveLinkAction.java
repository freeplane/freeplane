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
package org.freeplane.features.link.mindmapmode;

import java.awt.event.ActionEvent;
import java.net.URI;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

class RemoveLinkAction extends AFreeplaneAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public RemoveLinkAction() {
		super("RemoveLinkAction");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
	    final ModeController modeController = Controller.getCurrentModeController();
	    final MLinkController linkController = (MLinkController) LinkController.getController();
	    final NodeModel selectedNode = modeController.getMapController().getSelectedNode();
	    linkController.setLink(selectedNode, (URI) null, LinkController.LINK_ABSOLUTE);
	}
}
