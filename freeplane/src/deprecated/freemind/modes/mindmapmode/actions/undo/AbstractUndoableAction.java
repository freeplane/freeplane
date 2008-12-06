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

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.freeplane.modes.ModeControllerAction;
import org.freeplane.modes.mindmapmode.MModeController;

/**
 * @author foltin
 */
public abstract class AbstractUndoableAction extends ModeControllerAction {
	private IActor actor;

	protected AbstractUndoableAction(final MModeController controller,
	                                 final String name, final String icon) {
		super(controller, name, icon);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent arg0) {
		getActionFactory().startTransaction(
		    (String) getValue(Action.SHORT_DESCRIPTION));
		undoableActionPerformed(arg0);
		getActionFactory().endTransaction(
		    (String) getValue(Action.SHORT_DESCRIPTION));
	}

	@Override
	public void addActor(final IActor actor) {
		this.actor = actor;
		if (actor != null) {
			getActionFactory().registerActor(actor, actor.getDoActionClass());
		}
	}

	/**
	 *
	 */
	private ActionFactory getActionFactory() {
		return getMModeController().getActionFactory();
	}

	/**
	 */
	public IActor getActor() {
		return actor;
	}

	/**
	 */
	protected abstract void undoableActionPerformed(ActionEvent arg0);
}
