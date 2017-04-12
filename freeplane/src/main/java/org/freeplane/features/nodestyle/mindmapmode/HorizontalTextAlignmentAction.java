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

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel.HorizontalTextAlignment;

@SelectableAction(checkOnNodeChange = true)
class HorizontalTextAlignmentAction extends AMultipleNodeAction {
	private final HorizontalTextAlignment textAlignment;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean textAlignSet;

	/**
	 */
	public HorizontalTextAlignmentAction(HorizontalTextAlignment textAlignment) {
		super("TextAlignAction." + textAlignment);
		this.textAlignment =textAlignment;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		textAlignSet = !isTextAlignmentSet();
		super.actionPerformed(e);
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel selected) {
		((MNodeStyleController) NodeStyleController.getController()).setHorizontalTextAlignment(selected, textAlignSet ? textAlignment : null);
	}

	boolean isTextAlignmentSet() {
		final NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		return textAlignment.equals(NodeStyleController.getController().getHorizontalTextAlignment(node));
	}

	@Override
	public void setSelected() {
		setSelected(isTextAlignmentSet());
	}
}
