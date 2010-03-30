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
package org.freeplane.core.frame;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;

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
		super("ToggleFullScreenAction", viewController.getController());
	}

	public void actionPerformed(final ActionEvent e) {
		final ViewController viewController = getController().getViewController();
		viewController.setFullScreen(!viewController.isFullScreenEnabled());
	}

	@Override
	public void setSelected() {
		final ViewController viewController = getController().getViewController();
		setSelected(viewController.isFullScreenEnabled());
	}
}
