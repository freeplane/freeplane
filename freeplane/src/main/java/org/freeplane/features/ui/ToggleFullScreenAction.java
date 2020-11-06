/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.features.ui;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.menubuilders.generic.UserRole;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 * 14.08.2009
 */
@SelectableAction(checkOnPopup = true)
public class ToggleFullScreenAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ToggleFullScreenAction(final ViewController viewController) {
		super("ToggleFullScreenAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final FrameController viewController = (FrameController) Controller.getCurrentController().getViewController();
		viewController.setFullScreen(!viewController.isFullScreenEnabled());
	}

	@Override
	public void setSelected() {
		final FrameController viewController = (FrameController) Controller.getCurrentController().getViewController();
		setSelected(viewController.isFullScreenEnabled());
	}
	
	@Override
	public void afterMapChange(UserRole userRole) {
	}
}
