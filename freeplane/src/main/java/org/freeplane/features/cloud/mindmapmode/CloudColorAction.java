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
package org.freeplane.features.cloud.mindmapmode;

import java.awt.Color;
import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.ColorTracker;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.cloud.CloudController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;

class CloudColorAction extends AMultipleNodeAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Color actionColor;

	public CloudColorAction() {
		super("CloudColorAction");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		Color selectedColor = null;
		ModeController controller;
		{
			controller = Controller.getCurrentModeController();
			final NodeModel selected = controller.getMapController().getSelectedNode();
			final MCloudController cloudController = (MCloudController) CloudController
			    .getController();
			cloudController.setCloud(selected, true);
			selectedColor = cloudController.getColor(selected, StyleOption.FOR_UNSELECTED_NODE);
		}
		actionColor = ColorTracker.showCommonJColorChooserDialog(controller.getController()
		    .getSelection().getSelected(), TextUtils.getText("choose_cloud_color"), selectedColor, CloudController.getStandardColor());
		if(actionColor != null)
			super.actionPerformed(e);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.modes.mindmapmode.actions.MultipleNodeAction#actionPerformed
	 * (freeplane.modes.NodeModel)
	 */
	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		final MCloudController cloudController = (MCloudController) CloudController.getController();
		cloudController.setColor(node, actionColor);
	}
}
