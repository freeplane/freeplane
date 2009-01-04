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
package org.freeplane.core.ui;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.core.modecontroller.ModeController;

/**
 * listener, that blocks the controler if the menu is active (PN) Take care!
 * This listener is also used for modelpopups (as for graphical links).
 */
public class ControllerPopupMenuListener implements PopupMenuListener {
	ModeController modeController;

	public ControllerPopupMenuListener(final ModeController modeController) {
		super();
		this.modeController = modeController;
	}

	public void popupMenuCanceled(final PopupMenuEvent e) {
		modeController.setBlocked(false);
	}

	public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
		modeController.setBlocked(false);
	}

	public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
		modeController.setBlocked(true);
	}
}
