/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.mode;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.IMapSelection;

class MoveToRootAction extends AFreeplaneAction {
	static final String NAME = "moveToRoot";
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	MoveToRootAction() {
		super("MoveToRootAction");
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		Controller ctrl = Controller.getCurrentController();
		final IMapSelection selection = ctrl.getSelection();
		if (selection != null) {
			final Component selectedComponent = ctrl.getMapViewManager().getSelectedComponent();
			if (!selectedComponent.hasFocus() && java.awt.EventQueue.getCurrentEvent() instanceof KeyEvent)
				selectedComponent.requestFocusInWindow();
			else
				selection.selectRoot();
		}
	}
}
