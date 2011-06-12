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
package org.freeplane.view.swing.addins.filepreview;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.icon.mindmapmode.ProgressUtilities;
import org.freeplane.features.map.NodeModel;

/**
 * 
 * @author Stefan Ott
 *
 *This action adds an external image to a node
 */
@EnabledAction(checkOnNodeChange = true)
public class AddExternalImageAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public AddExternalImageAction() {
		super("ExternalImageAddAction");
	}

	public void actionPerformed(final ActionEvent arg0) {
		final ProgressUtilities progUtil = new ProgressUtilities();
		final List<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		final ViewerController vc = ((ViewerController) Controller.getCurrentController().getModeController()
		    .getExtension(ViewerController.class));
		final ExternalResource extRes = (ExternalResource) vc.createExtension(nodes.get(0));
		if (extRes != null) {
			final File file = new File(extRes.getAbsoluteUri(nodes.get(0).getMap()));
			for (final NodeModel node : nodes) {
				if (!progUtil.hasExternalResource(node)) {
					vc.paste(file, node, node.isLeft());
				}
			}
		}
	}

	@Override
	public void setEnabled() {
		boolean enable = false;
		final ProgressUtilities progUtil = new ProgressUtilities();
		final List<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		for (final NodeModel node : nodes) {
			if (node != null && !progUtil.hasExternalResource(node)) {
				enable = true;
				break;
			}
		}
		setEnabled(enable);
	}
}
