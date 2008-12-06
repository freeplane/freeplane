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

import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.modes.mindmapmode.actions.instance.ActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.instance.UnregistryAttributeValueActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.undo.AbstractActor;
import deprecated.freemind.modes.mindmapmode.actions.undo.ActionPair;

public class UnregistryAttributeValueActor extends AbstractActor {
	public UnregistryAttributeValueActor(
	                                     final MModeController mindMapModeController) {
		super(mindMapModeController);
	}

	public void act(final ActionInstance action) {
		if (action instanceof UnregistryAttributeValueActionInstance) {
			final UnregistryAttributeValueActionInstance unrregistryAttributeValueAction = (UnregistryAttributeValueActionInstance) action;
			act(unrregistryAttributeValueAction.getName(),
			    unrregistryAttributeValueAction.getValue());
		}
	}

	private void act(final String name, final String value) {
		getAttributeRegistry().getElement(name).removeValue(value);
	}

	public ActionInstance createAction(final String name, final String value) {
		final UnregistryAttributeValueActionInstance action = new UnregistryAttributeValueActionInstance();
		action.setName(name);
		action.setValue(value);
		return action;
	}

	public ActionPair createActionPair(final String name, final String value) {
		final ActionPair actionPair = new ActionPair(
		    createAction(name, value),
		    ((MAttributeController) getAttributeController()).registryAttributeValueActor
		        .createAction(name, value));
		return actionPair;
	}

	public Class getDoActionClass() {
		return UnregistryAttributeValueActionInstance.class;
	}
}
