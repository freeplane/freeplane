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
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.icon.ProgressUtilities;

/**
 * 
 * @author Stefan Ott
 *
 *This action removes external resources from nodes
 */
@EnabledAction(checkOnNodeChange = true)
public class RemoveExternalImageAction extends AMultipleNodeAction {
	private static final long serialVersionUID = 1L;

	public RemoveExternalImageAction() {
		super("ExternalImageRemoveAction");
	}

	@Override
	public void actionPerformed(final ActionEvent arg0, final NodeModel node) {
		final ProgressUtilities progUtil = new ProgressUtilities();
		final ViewerController vc = ((ViewerController) Controller.getCurrentController().getModeController()
		    .getExtension(ViewerController.class));
		if (progUtil.hasExternalResource(node) && !progUtil.hasExtendedProgressIcon(node)) {
			vc.undoableDeactivateHook(node);
		}
	}

	@Override
	public void setEnabled() {
		boolean enable = false;
		final ProgressUtilities progUtil = new ProgressUtilities();
		final List<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		for (final NodeModel node : nodes) {
			if (node != null && progUtil.hasExternalResource(node) && !progUtil.hasExtendedProgressIcon(node)) {
				enable = true;
				break;
			}
		}
		setEnabled(enable);
	}
}
