/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Stefan Ott in 2011.
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
package org.freeplane.view.swing.features.filepreview;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.Collection;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 *
 * @author Stefan Ott
 *
 *This action adds an external image to a node
 */
public class AddExternalImageAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public AddExternalImageAction() {
		super("ExternalImageAddAction");
	}

	public void actionPerformed(final ActionEvent event) {
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		final Collection<NodeModel> nodes = mapController.getSelectedNodes();
		final ViewerController vc = Controller.getCurrentController().getModeController()
		    .getExtension(ViewerController.class);
		final NodeModel selectedNode = mapController.getSelectedNode();
		if (selectedNode == null)
			return;
		final ExternalResource extRes = (ExternalResource) vc.createExtension(selectedNode);
		if (extRes == null)
			return;
		URI absoluteUri = extRes.getAbsoluteUri(selectedNode.getMap());
		if (absoluteUri == null)
			return;
		for (final NodeModel node : nodes) {
			vc.paste(absoluteUri, node, node.isLeft());
		}
	}
}
