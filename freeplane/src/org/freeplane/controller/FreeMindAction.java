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

import org.freeplane.ui.FreemindMenuBar;

/**
 * @author Dimitry Polivaev
 */
public abstract class FreeMindAction extends AbstractAction {
	public FreeMindAction(final ActionDescriptor descriptor) {
		this(descriptor.name(), descriptor.iconPath());
	}

	/**
	 * @param controller
	 * @param string
	 */
	public FreeMindAction(final String string) {
		this(string, null);
	}

	/**
	 * @param title
	 *            Title is a resource.
	 * @param iconPath
	 *            is a path to an icon.
	 */
	public FreeMindAction(final String title, final String iconPath) {
		super();
		if (title != null && !title.equals("")) {
			FreemindMenuBar.setLabelAndMnemonic(this, Freeplane.getController()
			    .getResourceController().getResourceString(title));
		}
		if (iconPath != null && !iconPath.equals("")) {
			final ImageIcon icon = new ImageIcon(Freeplane.getController()
			    .getResourceController().getResource(iconPath));
			putValue(Action.SMALL_ICON, icon);
		}
	}
}
