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
package org.freeplane.modes.mindmapmode;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeplaneAction;
import org.freeplane.controller.views.IMapViewChangeListener;
import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.mindmapmode.MindMapMapModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.ui.AlwaysEnabledAction;
import org.freeplane.undo.IUndoHandler;

@AlwaysEnabledAction
class UndoAction extends FreeplaneAction implements IMapViewChangeListener {
	private Action redo;

	public UndoAction() {
		super("undo", "images/undo.png");
		Controller.getController().getMapViewManager()
		    .addMapViewChangeListener(this);
		setEnabled(false);
	}

	public void actionPerformed(final ActionEvent e) {
		final MindMapMapModel map = (MindMapMapModel) Controller
		    .getController().getMap();
		final IUndoHandler undoHandler = map.getUndoHandler();
		undoHandler.getUndoAction().actionPerformed(e);
		setEnabled(undoHandler.canUndo());
		redo.setEnabled(undoHandler.canRedo());
	}

	public void afterMapClose(final MapView oldMapView) {
	}

	public void afterMapViewChange(final MapView oldMapView,
	                               final MapView newMapView) {
		if (newMapView == null) {
			return;
		}
		final MapModel map = newMapView.getModel();
		if (map instanceof MindMapMapModel) {
			final IUndoHandler undoHandler = ((MindMapMapModel) map)
			    .getUndoHandler();
			setEnabled(undoHandler.canUndo());
			redo.setEnabled(undoHandler.canRedo());
		}
	}

	public void beforeMapViewChange(final MapView oldMapView,
	                                final MapView newMapView) {
	}

	public boolean isMapViewChangeAllowed(final MapView oldMapView,
	                                      final MapView newMapView) {
		return true;
	}

	public void setRedo(final Action redo) {
		this.redo = redo;
	}
}
