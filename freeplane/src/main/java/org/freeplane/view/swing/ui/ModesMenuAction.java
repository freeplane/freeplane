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
package org.freeplane.view.swing.ui;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.menubuilders.generic.UserRole;
import org.freeplane.features.mode.Controller;

@SelectableAction
class ModesMenuAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private final String mode;

	public ModesMenuAction(final String mode, final Controller controller) {
		super("ModesMenuAction." + mode);
		this.mode = mode;
	}

	public void actionPerformed(final ActionEvent e) {
		Controller.getCurrentController().selectMode(mode);
	}

	@Override
	public void afterMapChange(UserRole userRole) {
	}
}
