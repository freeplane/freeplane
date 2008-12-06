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

import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.undo.IUndoableActor;

public class UndoActionHandler {
	final private MModeController controller;
	private boolean isUndoActionRunning;

	/**
	 */
	public UndoActionHandler(final MModeController controller) {
		this.controller = controller;
		isUndoActionRunning = false;
	}

	public void endTransaction(final String name) {
	}

	public void executeAction(final ActionPair pair) {
		if (!isUndoActionRunning()) {
			controller.execute(new IUndoableActor() {
				public void act() {
					isUndoActionRunning = true;
					controller.getActionFactory().startTransaction(
					    this.getClass().getName());
					controller.getActionFactory()
					    .executeAction(
					        new ActionPair(pair.getDoAction(), pair
					            .getUndoAction()));
					controller.getActionFactory().endTransaction(
					    this.getClass().getName());
					isUndoActionRunning = false;
				}

				public String getDescription() {
					return pair.getDoAction().getClass().toString();
				}

				public void undo() {
					isUndoActionRunning = true;
					controller.getActionFactory().startTransaction(
					    this.getClass().getName());
					controller.getActionFactory()
					    .executeAction(
					        new ActionPair(pair.getUndoAction(), pair
					            .getDoAction()));
					controller.getActionFactory().endTransaction(
					    this.getClass().getName());
					isUndoActionRunning = false;
				}
			});
		}
	}

	/**
	 * @return
	 */
	public boolean isUndoActionRunning() {
		return isUndoActionRunning;
	}

	public void startTransaction(final String name) {
	}
}
