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
package org.freeplane.features.common.text;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.text.MTextController;

@SelectableAction(checkOnNodeChange=true)
class ToggleDetailsAction extends AMultipleNodeAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean isHidden;

	public ToggleDetailsAction() {
		super("ToggleDetailsAction");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		isHidden = !isHidden();
		super.actionPerformed(e);
	}
	
	private boolean isHidden() {
		final NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		if(node == null){
			return false;
		}
		final DetailTextModel detailText = DetailTextModel.getDetailText(node);
		return detailText != null ? detailText.isHidden() : isHidden;
    }

	@Override
    protected void actionPerformed(ActionEvent e, NodeModel node) {
		final DetailTextModel detailText = DetailTextModel.getDetailText(node);
		if(detailText == null){
			return;
		}
		MTextController controller = (MTextController) MTextController.getController();
		controller.setDetailsHidden(node, isHidden);
    }

	@Override
	public void setSelected() {
		setSelected(!isHidden());
	}
}
