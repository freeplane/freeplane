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
import deprecated.freemind.modes.mindmapmode.actions.instance.SetAttributeFontSizeActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.undo.AbstractActor;
import deprecated.freemind.modes.mindmapmode.actions.undo.ActionPair;

public class SetAttributeFontSizeActor extends AbstractActor {
	public SetAttributeFontSizeActor(final MModeController mindMapModeController) {
		super(mindMapModeController);
	}

	public void act(final ActionInstance action) {
		if (action instanceof SetAttributeFontSizeActionInstance) {
			final SetAttributeFontSizeActionInstance setAttributeFontSizeAction = (SetAttributeFontSizeActionInstance) action;
			act(setAttributeFontSizeAction.getSize());
		}
	}

	private void act(final int size) {
		getAttributeRegistry().setFontSize(size);
	}

	public ActionInstance createAction(final int size) {
		final SetAttributeFontSizeActionInstance action = new SetAttributeFontSizeActionInstance();
		action.setSize(size);
		return action;
	}

	public ActionPair createActionPair(final int size) {
		final int previousSize = getAttributeRegistry().getFontSize();
		final ActionPair actionPair = new ActionPair(createAction(size),
		    createAction(previousSize));
		return actionPair;
	}

	public Class getDoActionClass() {
		return SetAttributeFontSizeActionInstance.class;
	}
}
