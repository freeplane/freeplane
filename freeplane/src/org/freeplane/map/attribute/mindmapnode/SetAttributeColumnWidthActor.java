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

import org.freeplane.map.attribute.NodeAttributeTableModel;
import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.modes.mindmapmode.actions.instance.ActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.instance.SetAttributeColumnWidthActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.undo.AbstractActor;
import deprecated.freemind.modes.mindmapmode.actions.undo.ActionPair;

public class SetAttributeColumnWidthActor extends AbstractActor {
	public SetAttributeColumnWidthActor(
	                                    final MModeController mindMapModeController) {
		super(mindMapModeController);
	}

	public void act(final ActionInstance action) {
		if (action instanceof SetAttributeColumnWidthActionInstance) {
			final SetAttributeColumnWidthActionInstance setAttributeColumnWidthAction = (SetAttributeColumnWidthActionInstance) action;
			act(getNode(setAttributeColumnWidthAction.getNode())
			    .getAttributes(), setAttributeColumnWidthAction.getColumn(),
			    setAttributeColumnWidthAction.getWidth());
		}
	}

	private void act(final NodeAttributeTableModel model, final int col,
	                 final int width) {
		model.getLayout().setColumnWidth(col, width);
	}

	public ActionInstance createAction(final NodeAttributeTableModel model,
	                                   final int col, final int width) {
		final SetAttributeColumnWidthActionInstance action = new SetAttributeColumnWidthActionInstance();
		action.setNode(getNodeID(model.getNode()));
		action.setColumn(col);
		action.setWidth(width);
		return action;
	}

	public ActionPair createActionPair(final NodeAttributeTableModel model,
	                                   final int col, final int width) {
		final int previousWidth = model.getColumnWidth(col);
		final ActionPair actionPair = new ActionPair(createAction(model, col,
		    width), createAction(model, col, previousWidth));
		return actionPair;
	}

	public Class getDoActionClass() {
		return SetAttributeColumnWidthActionInstance.class;
	}
}
