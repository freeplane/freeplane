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

import org.freeplane.api.ChildNodesLayout;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.layout.LayoutModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 */
class ChangeChildNodesLayoutActor implements IActor {
	private final NodeModel node;
	private final ChildNodesLayout oldSides;
	private final ChildNodesLayout newSides;

	ChangeChildNodesLayoutActor(final NodeModel node, ChildNodesLayout newSides){
		final LayoutModel layoutModel = LayoutModel.getModel(node);
		oldSides = layoutModel.getChildNodesLayout();
		this.node = node;
		this.newSides = newSides;
	}

	@Override
    public void act() {
		setSides(node, oldSides, newSides);
	}

	@Override
    public String getDescription() {
		return "changeChildNodesLayout";
	}

	private void setSides(final NodeModel node, ChildNodesLayout oldSides, ChildNodesLayout newSides) {
		if(oldSides != newSides) {
			LayoutModel.createLayoutModel(node).setChildNodesLayout(newSides);
			Controller.getCurrentModeController().getMapController()
			.nodeChanged(node, ChildNodesLayout.class, oldSides, newSides);
		}
	}

	@Override
    public void undo() {
		setSides(node, newSides, oldSides);
	}
}