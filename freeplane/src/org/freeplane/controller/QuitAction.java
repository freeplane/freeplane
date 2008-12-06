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
package org.freeplane.controller;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.freeplane.controller.resources.ResourceController;
import org.freeplane.ui.AlwaysEnabledAction;
import org.freeplane.ui.FreemindMenuBar;

/**
 * Manages the history of visited maps. Maybe explicitly closed maps should be
 * removed from History too?
 */
@AlwaysEnabledAction
class QuitAction extends AbstractAction {
	/**
	 * @param resourceController
	 */
	QuitAction(final ResourceController resourceController) {
		FreemindMenuBar.setLabelAndMnemonic(this, resourceController
		    .getResourceString("quit"));
	}

	public void actionPerformed(final ActionEvent e) {
		Freeplane.getController().quit();
	}
}
