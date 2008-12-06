/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file author is Christian Foltin
 *  It is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.modes;

import javax.swing.ImageIcon;

import org.freeplane.controller.FreeMindAction;
import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.modes.mindmapmode.actions.undo.IActor;

/**
 * @author foltin
 */
/**
 * @author foltin
 */
public abstract class ModeControllerAction extends FreeMindAction {
	final private ModeController modeController;

	public ModeControllerAction(final MModeController modeController,
	                            final String title, final ImageIcon icon) {
		super(title, null);
		putValue(SMALL_ICON, icon);
		this.modeController = modeController;
	}

	/**
	 * @param controller
	 * @param string
	 */
	public ModeControllerAction(final ModeController controller,
	                            final String string) {
		this(controller, string, null);
	}

	/**
	 * @param title
	 *            Title is a resource.
	 * @param iconPath
	 *            is a path to an icon.
	 */
	public ModeControllerAction(final ModeController modeController,
	                            final String title, final String iconPath) {
		super(title, iconPath);
		this.modeController = modeController;
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
		return (MModeController) modeController;
	}

	public ModeController getModeController() {
		return modeController;
	}
}
