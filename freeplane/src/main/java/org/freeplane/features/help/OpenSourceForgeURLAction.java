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
package org.freeplane.features.help;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;


import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog.MessageType;

/**
 * @author Dimitry Polivaev
 * 18.05.2009
 */

public class OpenSourceForgeURLAction extends OpenURLAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	OpenSourceForgeURLAction(final String key, final String url) {
		super(key, url);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final int showResult = OptionalDontShowMeAgainDialog.show("sf_login_required", "confirmation",
		    "open_source_forge_url", MessageType.ONLY_OK_SELECTION_IS_STORED);
		if (showResult != JOptionPane.OK_OPTION) {
			return;
		}
		super.actionPerformed(e);
	}
}
