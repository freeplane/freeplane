/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.features.nodelocation.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodelocation.LocationController;
import org.freeplane.features.nodelocation.LocationModel;

/**
 * @author Dimitry Polivaev
 * 07.12.2008
 */
class ResetNodeLocationAction extends AMultipleNodeAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public ResetNodeLocationAction() {
		super("ResetNodeLocationAction");
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		MLocationController locationController = (MLocationController) LocationController.getController();
		locationController.moveNodePosition(node, LocationModel.NULL_LOCATION.getHGap(), LocationModel.NULL_LOCATION
						.getShiftY());
		locationController.setBaseHGapToChildren(node.getParentNode(), LocationModel.NULL_LOCATION.getBaseHGap());
		locationController.setCommonVGapBetweenChildren(node.getParentNode(), LocationModel.NULL_LOCATION.getVGap());
	}
}
