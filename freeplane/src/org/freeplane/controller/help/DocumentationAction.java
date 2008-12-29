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
package org.freeplane.controller.help;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.freeplane.controller.Controller;
import org.freeplane.controller.resources.ResourceController;
import org.freeplane.modes.browsemode.BModeController;
import org.freeplane.ui.MenuBuilder;

class DocumentationAction extends AbstractAction {
	DocumentationAction() {
		MenuBuilder.setLabelAndMnemonic(this, Controller.getText("documentation"));
	}

	public void actionPerformed(final ActionEvent e) {
		String map = Controller.getText("browsemode_initial_map");
		map = ResourceController.removeTranslateComment(map);
		if (map != null && map.startsWith(".")) {
			try {
				map = Controller.getController().getHelpController().convertLocalLink(map);
			}
			catch (final AccessControlException ex) {
				Controller.getController().getHelpController().webDocu(e);
				return;
			}
		}
		if (map != null && map != "") {
			URL url = null;
			try {
				url = new URL(map);
			}
			catch (final MalformedURLException e2) {
				org.freeplane.Tools.logException(e2);
				return;
			}
			final URL endUrl = url;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						if (Controller.getController().selectMode(BModeController.MODENAME)) {
							Controller.getController();
							((BModeController) Controller.getModeController()).getMapController()
							    .newMap(endUrl);
						}
					}
					catch (final Exception e1) {
						org.freeplane.Tools.logException(e1);
					}
				}
			});
		}
	}
}
