/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is to be deleted.
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
package deprecated.freemind.modes.mindmapmode.actions.undo;

import org.freeplane.controller.Freeplane;
import org.freeplane.map.attribute.AttributeRegistry;
import org.freeplane.map.attribute.IAttributeController;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.modes.mindmapmode.actions.instance.CompoundActionInstance;

public abstract class AbstractActor implements IActor {
	final private MModeController mindMapModeController;

	protected AbstractActor(final MModeController mindMapModeController) {
		this.mindMapModeController = mindMapModeController;
		mindMapModeController.getActionFactory().registerActor(this,
		    getDoActionClass());
	}

	protected CompoundActionInstance createCompoundAction() {
		return new CompoundActionInstance();
	}

	protected IAttributeController getAttributeController() {
		return mindMapModeController.getAttributeController();
	}

	protected AttributeRegistry getAttributeRegistry() {
		return Freeplane.getController().getMap().getRegistry().getAttributes();
	}

	protected NodeModel getNode(final String nodeID) {
		return mindMapModeController.getMapController().getNodeFromID(nodeID);
	}

	protected String getNodeID(final NodeModel node) {
		return mindMapModeController.getMapController().getNodeID(node);
	}
}
