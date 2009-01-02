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
package org.freeplane.map.nodestyle.mindmapmode;

import java.awt.Color;
import java.awt.event.ActionEvent;

import org.freeplane.core.map.NodeModel;
import org.freeplane.core.ui.MultipleNodeAction;
import org.freeplane.map.nodestyle.NodeStyleController;
import org.freeplane.map.nodestyle.NodeStyleModel;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.view.swing.map.MapView;

/**
 * @author foltin
 */
class NodeColorBlendAction extends MultipleNodeAction {
	/**
	 *
	 */
	public NodeColorBlendAction() {
		super("blend_color");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.modes.mindmapmode.actions.MultipleNodeAction#actionPerformed
	 * (freemind.modes.NodeModel)
	 */
	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		final Color mapColor = MModeController.getMModeController().getMapView().getBackground();
		Color nodeColor = NodeStyleModel.getColor(node);
		if (nodeColor == null) {
			nodeColor = MapView.standardNodeTextColor;
		}
		((MNodeStyleController) NodeStyleController.getController(MModeController.getMModeController())).setColor(node,
		    new Color((3 * mapColor.getRed() + nodeColor.getRed()) / 4,
		        (3 * mapColor.getGreen() + nodeColor.getGreen()) / 4,
		        (3 * mapColor.getBlue() + nodeColor.getBlue()) / 4));
	}
}
