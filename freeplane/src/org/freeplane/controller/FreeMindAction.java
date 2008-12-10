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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.freeplane.modes.ModeController;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.ui.MenuBuilder;

import deprecated.freemind.modes.mindmapmode.actions.undo.IActor;

/**
 * @author Dimitry Polivaev
 */
public abstract class FreeMindAction extends AbstractAction {
	public FreeMindAction() {
		super();
	}

	public FreeMindAction(final ActionDescriptor descriptor) {
		this(descriptor.name(), descriptor.iconPath());
	}

	/**
	 * @param controller
	 * @param string
	 */
	public FreeMindAction(final String title) {
		this();
		if (title != null && !title.equals("")) {
			MenuBuilder.setLabelAndMnemonic(this, Controller.getText(title));
		}
	}

	public FreeMindAction(final String title, final ImageIcon icon) {
		this(title);
		putValue(SMALL_ICON, icon);
	}

	/**
	 * @param title
	 *            Title is a resource.
	 * @param iconPath
	 *            is a path to an icon.
	 */
	public FreeMindAction(final String title, final String iconPath) {
		this(title);
		if (iconPath != null && !iconPath.equals("")) {
			final ImageIcon icon = new ImageIcon(Controller
			    .getResourceController().getResource(iconPath));
			putValue(Action.SMALL_ICON, icon);
		}
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public void addActor(final IActor actor) {
		getMModeController().getActionFactory().registerActor(actor,
		    actor.getDoActionClass());
	}

	public MModeController getMModeController() {
		return (MModeController) Controller.getController().getModeController(
		    MModeController.MODENAME);
	}

	public ModeController getModeController() {
		return Controller.getController().getModeController();
	}
}
