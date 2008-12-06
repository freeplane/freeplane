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
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.service.filter.util.SortedComboBoxModel;

import deprecated.freemind.modes.mindmapmode.actions.instance.ActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.instance.CompoundActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.instance.UnregistryAttributeActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.undo.AbstractActor;
import deprecated.freemind.modes.mindmapmode.actions.undo.ActionPair;

public class UnregistryAttributeActor extends AbstractActor {
	public UnregistryAttributeActor(final MModeController mindMapModeController) {
		super(mindMapModeController);
	}

	public void act(final ActionInstance action) {
		if (action instanceof UnregistryAttributeActionInstance) {
			final UnregistryAttributeActionInstance unregistryAttributeElementaryAction = (UnregistryAttributeActionInstance) action;
			act(unregistryAttributeElementaryAction.getName());
		}
	}

	private void act(final String name) {
		final AttributeRegistry registry = getAttributeRegistry();
		registry.unregistry(name);
	}

	public ActionInstance createAction(final String name) {
		final UnregistryAttributeActionInstance action = new UnregistryAttributeActionInstance();
		action.setName(name);
		return action;
	}

	public ActionPair createActionPair(final String name) {
		final ActionPair actionPair = new ActionPair(createAction(name),
		    createUndoAction(name));
		return actionPair;
	}

	private ActionInstance createUndoAction(final String name) {
		final CompoundActionInstance compoundAction = createCompoundAction();
		final SortedComboBoxModel values = getAttributeRegistry().getElement(
		    name).getValues();
		final ActionInstance firstAction = ((MAttributeController) getAttributeController()).registryAttributeActor
		    .createAction(name);
		compoundAction.addChoice(firstAction);
		for (int i = 0; i < values.getSize(); i++) {
			final String value = values.getElementAt(i).toString();
			final ActionInstance nextAction = ((MAttributeController) getAttributeController()).registryAttributeValueActor
			    .createAction(name, value);
			compoundAction.addChoice(nextAction);
		}
		return compoundAction;
	}

	public Class getDoActionClass() {
		return UnregistryAttributeActionInstance.class;
	}
}
