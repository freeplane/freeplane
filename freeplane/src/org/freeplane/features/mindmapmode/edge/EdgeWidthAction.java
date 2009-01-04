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
package org.freeplane.features.mindmapmode.edge;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.MultipleNodeAction;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.edge.EdgeModel;

class EdgeWidthAction extends MultipleNodeAction {
	private static String getWidthTitle(final ModeController controller, final int width) {
		String returnValue;
		if (width == EdgeModel.WIDTH_PARENT) {
			returnValue = ("edge_width_parent");
		}
		else if (width == EdgeModel.WIDTH_THIN) {
			returnValue = controller.getText("edge_width_thin");
		}
		else {
			returnValue = Integer.toString(width);
		}
		return /* controller.getText("edge_width") + */returnValue;
	}

	final private int mWidth;

	public EdgeWidthAction(final ModeController controller, final int width) {
		super(null);
		mWidth = width;
		putValue(Action.NAME, EdgeWidthAction.getWidthTitle(controller, width));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.modes.mindmapmode.actions.MultipleNodeAction#actionPerformed
	 * (freemind.modes.NodeModel)
	 */
	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		((MEdgeController) EdgeController.getController(getModeController()))
		    .setWidth(node, mWidth);
	}
}
