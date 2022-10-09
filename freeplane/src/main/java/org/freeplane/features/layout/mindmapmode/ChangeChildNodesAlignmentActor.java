/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.features.layout.mindmapmode;

import org.freeplane.api.ChildNodesAlignment;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.layout.LayoutModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 */
class ChangeChildNodesAlignmentActor implements IActor {
	private final NodeModel node;
	private final ChildNodesAlignment oldAlignment;
	private final ChildNodesAlignment newAlignment;

	ChangeChildNodesAlignmentActor(final NodeModel node, ChildNodesAlignment newAlignment){
		final LayoutModel layoutModel = LayoutModel.getModel(node);
		oldAlignment = layoutModel.getChildNodesAlignment();
		this.node = node;
		this.newAlignment = newAlignment;
	}

	@Override
    public void act() {
		setAlignment(node, oldAlignment, newAlignment);
	}

	@Override
    public String getDescription() {
		return "changeChildNodesAlignment";
	}

	private void setAlignment(final NodeModel node, ChildNodesAlignment oldAlignment, ChildNodesAlignment newAlignment) {
		if(oldAlignment != newAlignment) {
			LayoutModel.createLayoutModel(node).setChildNodesAlignment(newAlignment);
			Controller.getCurrentModeController().getMapController()
			.nodeChanged(node, ChildNodesAlignment.class, oldAlignment, newAlignment);
		}
	}

	@Override
    public void undo() {
		setAlignment(node, newAlignment, oldAlignment);
	}
}