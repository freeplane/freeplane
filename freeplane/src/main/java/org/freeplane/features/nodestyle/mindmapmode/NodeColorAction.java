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
package org.freeplane.features.nodestyle.mindmapmode;

import java.awt.Color;
import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.ColorTracker;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;

class NodeColorAction extends AMultipleNodeAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Color actionColor;

	public NodeColorAction() {
		super("NodeColorAction");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		actionColor = ColorTracker.showCommonJColorChooserDialog(Controller.getCurrentController().getSelection()
		    .getSelected(), 
		    TextUtils.getText("choose_node_color"), 
		    NodeStyleModel.getColor(Controller.getCurrentModeController().getMapController().getSelectedNode()), 
		    NodeStyleController.standardNodeTextColor);
		if(actionColor != null)
			super.actionPerformed(e);
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		((MNodeStyleController) NodeStyleController.getController()).setColor(node, actionColor);
	}
}
