/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 home
 *
 *  This file author is home
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
package org.freeplane.features.styles.mindmapmode;

import java.awt.event.ActionEvent;
import java.net.URI;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.view.swing.features.filepreview.ExternalResource;
import org.freeplane.view.swing.features.filepreview.ViewerController;

/**
 * Feb 1, 2014
 */
class MapBackgroundImageAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	MapBackgroundImageAction() {
		super("MapBackgroundImageAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		final ViewerController vc = Controller.getCurrentController().getModeController()
		    .getExtension(ViewerController.class);
		final NodeModel selectedNode = mapController.getSelectedNode();
		if (selectedNode == null) {
			return;
		}
		final ExternalResource extRes = (ExternalResource) vc.createExtension(selectedNode);
		if (extRes == null) {
			return;
		}
		final URI absoluteUri = extRes.getAbsoluteUri(selectedNode.getMap());
		if (absoluteUri == null) {
			return;
		}
		final Controller controller = Controller.getCurrentController();
		final MapStyle mapStyle = controller.getModeController().getExtension(MapStyle.class);
		final MapModel model = controller.getMap();
		mapStyle.setProperty(model, "backgroundImageURI", absoluteUri.toString());
	}
}
