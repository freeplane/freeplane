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
package org.freeplane.features.mode.mindmapmode;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.generic.UserRole;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

class RedoAction extends AFreeplaneAction{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Action undo;

	public RedoAction() {
		super("RedoAction");
		setEnabled(false);
	}

	public void actionPerformed(final ActionEvent e) {
		if(UITools.isEditingText())
			return;
		final Controller controller = Controller.getCurrentController();
		final MapModel map = controller.getMap();
		final IUndoHandler undoHandler = map.getExtension(IUndoHandler.class);
		undoHandler.getRedoAction().actionPerformed(e);
		undo.setEnabled(undoHandler.canUndo());
		setEnabled(undoHandler.canRedo());
	}

	public void reset() {
		final MapModel map = Controller.getCurrentController().getMap();
		final IUndoHandler undoHandler = map.getExtension(IUndoHandler.class);
		undoHandler.resetRedo();
		setEnabled(false);
	}

	public void setUndo(final Action undo) {
		this.undo = undo;
	}
	@Override
    public void afterMapChange(UserRole userRole) {}
}
