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

import org.freeplane.api.TextWritingDirection;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;

@SelectableAction(checkOnNodeChange = true)
class TextWritingDirectionAction extends AMultipleNodeAction {
	private final TextWritingDirection textDirection;
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private boolean textAlignSet;

	/**
	 */
	public TextWritingDirectionAction(TextWritingDirection textDirection) {
		super("TextWritingDirectionAction." + textDirection);
		this.textDirection =textDirection;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		textAlignSet = !isTextWritingDirectionSet();
		super.actionPerformed(e);
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel selected) {
		((MNodeStyleController) NodeStyleController.getController()).setTextWritingDirection(selected, textAlignSet ? textDirection : null);
	}

	boolean isTextWritingDirectionSet() {
		final NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		return textDirection.equals(NodeStyleController.getController().getTextWritingDirection(node, StyleOption.FOR_UNSELECTED_NODE));
	}

	@Override
	public void setSelected() {
		setSelected(isTextWritingDirectionSet());
	}
}
