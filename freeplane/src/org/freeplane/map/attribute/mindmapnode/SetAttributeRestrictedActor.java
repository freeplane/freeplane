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

import deprecated.freemind.modes.mindmapmode.actions.instance.ActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.instance.SetAttributeRestrictedActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.undo.AbstractActor;
import deprecated.freemind.modes.mindmapmode.actions.undo.ActionPair;

public class SetAttributeRestrictedActor extends AbstractActor {
	public SetAttributeRestrictedActor(
	                                   final MModeController mindMapModeController) {
		super(mindMapModeController);
	}

	public void act(final ActionInstance action) {
		if (action instanceof SetAttributeRestrictedActionInstance) {
			final SetAttributeRestrictedActionInstance setAttributeRestrictedAction = (SetAttributeRestrictedActionInstance) action;
			act(setAttributeRestrictedAction.getIndex(),
			    setAttributeRestrictedAction.getIsRestricted());
		}
	}

	private void act(final int index, final boolean value) {
		if (index == AttributeRegistry.GLOBAL) {
			getAttributeRegistry().setRestricted(value);
		}
		else {
			getAttributeRegistry().getElement(index).setRestriction(value);
		}
	}

	public ActionInstance createAction(final int index, final boolean value) {
		final SetAttributeRestrictedActionInstance action = new SetAttributeRestrictedActionInstance();
		action.setIndex(index);
		action.setIsRestricted(value);
		return action;
	}

	public ActionPair createActionPair(final int index, final boolean value) {
		final boolean previousValue;
		if (index == AttributeRegistry.GLOBAL) {
			previousValue = getAttributeRegistry().isRestricted();
		}
		else {
			previousValue = getAttributeRegistry().getElement(index)
			    .isRestricted();
		}
		final ActionPair actionPair = new ActionPair(
		    createAction(index, value), createAction(index, previousValue));
		return actionPair;
	}

	public Class getDoActionClass() {
		return SetAttributeRestrictedActionInstance.class;
	}
}
