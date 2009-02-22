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

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MultipleNodeAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.edge.EdgeModel;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;

@SelectableAction(checkOnNodeChange = true)
class HideEdgeAction extends MultipleNodeAction {
		private boolean hide;

		/**
		 */
		public HideEdgeAction(final Controller controller) {
			super(controller, "hide_edge");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			hide = !isHidden();
			super.actionPerformed(e);
		}

		@Override
		protected void actionPerformed(final ActionEvent e, final NodeModel selected) {
			((MEdgeController) EdgeController.getController(getModeController())).setHidden(selected, hide);
		}

		boolean isHidden() {
			final NodeModel node = getModeController().getMapController().getSelectedNode();
			return EdgeController.getController(getModeController()).isHidden(node);
		}

		@Override
		public void setSelected() {
			setSelected(isHidden());
		}
	}
