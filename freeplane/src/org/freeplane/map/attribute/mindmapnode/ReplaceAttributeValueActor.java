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
import deprecated.freemind.modes.mindmapmode.actions.instance.ReplaceAttributeValueActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.undo.AbstractActor;
import deprecated.freemind.modes.mindmapmode.actions.undo.ActionPair;

public class ReplaceAttributeValueActor extends AbstractActor {
	public ReplaceAttributeValueActor(
	                                  final MModeController mindMapModeController) {
		super(mindMapModeController);
	}

	public void act(final ActionInstance action) {
		if (action instanceof ReplaceAttributeValueActionInstance) {
			final ReplaceAttributeValueActionInstance replaceAttributeValueAction = (ReplaceAttributeValueActionInstance) action;
			act(replaceAttributeValueAction.getName(),
			    replaceAttributeValueAction.getOldValue(),
			    replaceAttributeValueAction.getNewValue());
		}
	}

	private void act(final String name, final String oldValue,
	                 final String newValue) {
		getAttributeRegistry().getElement(name)
		    .replaceValue(oldValue, newValue);
	}

	public ActionInstance createAction(final String name,
	                                   final String oldValue,
	                                   final String newValue) {
		final ReplaceAttributeValueActionInstance action = new ReplaceAttributeValueActionInstance();
		action.setName(name);
		action.setOldValue(oldValue);
		action.setNewValue(newValue);
		return action;
	}

	public ActionPair createActionPair(final String name,
	                                   final String oldValue,
	                                   final String newValue) {
		final ActionPair actionPair = new ActionPair(createAction(name,
		    oldValue, newValue), createAction(name, newValue, oldValue));
		return actionPair;
	}

	public Class getDoActionClass() {
		return ReplaceAttributeValueActionInstance.class;
	}
}
