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
package org.freeplane.map.attribute.mindmapnode;

import org.freeplane.map.attribute.AttributeRegistry;
import org.freeplane.map.attribute.AttributeRegistryElement;
import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.modes.mindmapmode.actions.instance.ActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.instance.RegistryAttributeActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.undo.AbstractActor;
import deprecated.freemind.modes.mindmapmode.actions.undo.ActionPair;

public class RegistryAttributeActor extends AbstractActor {
	public RegistryAttributeActor(final MModeController mindMapModeController) {
		super(mindMapModeController);
	}

	public void act(final ActionInstance action) {
		if (action instanceof RegistryAttributeActionInstance) {
			final RegistryAttributeActionInstance registryAttributeAction = (RegistryAttributeActionInstance) action;
			act(registryAttributeAction.getName());
		}
	}

	private void act(final String name) {
		final AttributeRegistry registry = getAttributeRegistry();
		final AttributeRegistryElement attributeRegistryElement = new AttributeRegistryElement(
		    registry, name);
		final int index = registry.getElements().add(name,
		    attributeRegistryElement);
		registry.getTableModel().fireTableRowsInserted(index, index);
	}

	public ActionInstance createAction(final String name) {
		final RegistryAttributeActionInstance action = new RegistryAttributeActionInstance();
		action.setName(name);
		return action;
	}

	public ActionPair createActionPair(final String name) {
		final ActionPair actionPair = new ActionPair(
		    createAction(name),
		    ((MAttributeController) getAttributeController()).unregistryAttributeActor
		        .createAction(name));
		return actionPair;
	}

	public Class getDoActionClass() {
		return RegistryAttributeActionInstance.class;
	}
}
