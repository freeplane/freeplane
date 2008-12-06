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

import org.freeplane.map.attribute.Attribute;
import org.freeplane.map.attribute.NodeAttributeTableModel;
import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.modes.mindmapmode.actions.instance.ActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.instance.InsertAttributeActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.undo.AbstractActor;
import deprecated.freemind.modes.mindmapmode.actions.undo.ActionPair;

public class InsertAttributeActor extends AbstractActor {
	public InsertAttributeActor(final MModeController mindMapModeController) {
		super(mindMapModeController);
	}

	public void act(final ActionInstance action) {
		if (action instanceof InsertAttributeActionInstance) {
			final InsertAttributeActionInstance insertAttributeAction = (InsertAttributeActionInstance) action;
			act(getNode(insertAttributeAction.getNode()).getAttributes(),
			    insertAttributeAction.getRow(),
			    insertAttributeAction.getName(), insertAttributeAction
			        .getValue());
		}
	}

	private void act(final NodeAttributeTableModel model, final int row,
	                 final String name, final String value) {
		final Attribute newAttribute = new Attribute(name, value);
		model.getAttributes().add(row, newAttribute);
		model.enableStateIcon();
		model.fireTableRowsInserted(row, row);
	}

	public ActionInstance createAction(final NodeAttributeTableModel model,
	                                   final int row, final String name,
	                                   final String value) {
		final InsertAttributeActionInstance action = new InsertAttributeActionInstance();
		action.setNode(getNodeID(model.getNode()));
		action.setRow(row);
		action.setName(name);
		action.setValue(value);
		return action;
	}

	public ActionPair createActionPair(final NodeAttributeTableModel model,
	                                   final int row, final String name,
	                                   final String value) {
		final ActionPair actionPair = new ActionPair(
		    createAction(model, row, name, value),
		    ((MAttributeController) getAttributeController()).removeAttributeActor
		        .createAction(model, row));
		return actionPair;
	}

	public Class getDoActionClass() {
		return InsertAttributeActionInstance.class;
	}
}
